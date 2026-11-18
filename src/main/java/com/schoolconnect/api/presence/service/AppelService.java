package com.schoolconnect.api.presence.service;

import com.schoolconnect.api.eleve.dto.EleveDtos.EleveView;
import com.schoolconnect.api.eleve.model.Eleve;
import com.schoolconnect.api.eleve.model.Inscription;
import com.schoolconnect.api.eleve.model.StatutInscription;
import com.schoolconnect.api.eleve.model.StatutParentEleve;
import com.schoolconnect.api.eleve.repository.EleveRepository;
import com.schoolconnect.api.eleve.repository.InscriptionRepository;
import com.schoolconnect.api.eleve.repository.ParentEleveRepository;
import com.schoolconnect.api.presence.dto.AppelDtos.AppelView;
import com.schoolconnect.api.presence.dto.AppelDtos.CorrigerAppelRequest;
import com.schoolconnect.api.presence.dto.AppelDtos.PresenceEleveView;
import com.schoolconnect.api.presence.dto.AppelDtos.PresenceHistoriqueView;
import com.schoolconnect.api.presence.dto.AppelDtos.PresenceSaisie;
import com.schoolconnect.api.presence.dto.AppelDtos.SaisirAppelRequest;
import com.schoolconnect.api.presence.model.Appel;
import com.schoolconnect.api.presence.model.PresenceEleve;
import com.schoolconnect.api.presence.model.StatutPresence;
import com.schoolconnect.api.presence.repository.AppelRepository;
import com.schoolconnect.api.presence.repository.PresenceEleveRepository;
import com.schoolconnect.api.school.repository.AnneeScolaireRepository;
import com.schoolconnect.api.school.repository.ClasseRepository;
import com.schoolconnect.api.school.repository.MatiereRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppelService {

    private final AppelRepository appels;
    private final PresenceEleveRepository presences;
    private final ClasseRepository classes;
    private final AnneeScolaireRepository annees;
    private final MatiereRepository matieres;
    private final InscriptionRepository inscriptions;
    private final EleveRepository eleves;
    private final ParentEleveRepository parentsEleves;

    public AppelService(AppelRepository appels, PresenceEleveRepository presences, ClasseRepository classes,
                        AnneeScolaireRepository annees, MatiereRepository matieres,
                        InscriptionRepository inscriptions, EleveRepository eleves,
                        ParentEleveRepository parentsEleves) {
        this.appels = appels;
        this.presences = presences;
        this.classes = classes;
        this.annees = annees;
        this.matieres = matieres;
        this.inscriptions = inscriptions;
        this.eleves = eleves;
        this.parentsEleves = parentsEleves;
    }

    /** CU-ENS-03 / CU-SUR-02 : faire l'appel d'une classe. Toute exception au PRESENT par défaut doit être listée. */
    @Transactional
    public AppelView saisirAppel(UUID etablissementId, UUID classeId, SaisirAppelRequest request, UUID saisiPar) {
        if (!classes.existsByIdAndEtablissementId(classeId, etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable pour cet établissement");
        }
        UUID anneeId = anneeCouranteId(etablissementId);
        if (appels.existsByClasseIdAndDateAppel(classeId, request.date())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un appel existe déjà pour cette classe à cette date ; utilisez la correction");
        }
        if (request.matiereId() != null && !matieres.existsByIdAndEtablissementId(request.matiereId(), etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Matière introuvable pour cet établissement");
        }

        List<Inscription> roster = inscriptions.findByClasseIdAndAnneeScolaireIdAndStatut(
                classeId, anneeId, StatutInscription.VALIDE);
        if (roster.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun élève inscrit dans cette classe pour l'année courante");
        }
        List<UUID> rosterIds = roster.stream().map(Inscription::getEleveId).toList();

        Map<UUID, PresenceSaisie> exceptions = new HashMap<>();
        for (PresenceSaisie saisie : request.presences()) {
            if (!rosterIds.contains(saisie.eleveId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "L'élève " + saisie.eleveId() + " n'est pas inscrit dans cette classe");
            }
            exceptions.put(saisie.eleveId(), saisie);
        }

        Appel appel = new Appel();
        appel.setEtablissementId(etablissementId);
        appel.setClasseId(classeId);
        appel.setAnneeScolaireId(anneeId);
        appel.setDateAppel(request.date());
        appel.setMatiereId(request.matiereId());
        appel.setSaisiPar(saisiPar);
        appel = appels.save(appel);

        for (UUID eleveId : rosterIds) {
            PresenceSaisie saisie = exceptions.get(eleveId);
            PresenceEleve presence = new PresenceEleve();
            presence.setAppelId(appel.getId());
            presence.setEleveId(eleveId);
            if (saisie != null) {
                presence.setStatut(saisie.statut());
                presence.setHeureArrivee(saisie.heureArrivee());
                presence.setHeureSortie(saisie.heureSortie());
            } else {
                presence.setStatut(StatutPresence.PRESENT);
            }
            presences.save(presence);
        }

        return toView(appel);
    }

    /** CU-SUR-02 (correction) / CU-SUR-04 (retards, sorties anticipées) : corrige des présences déjà saisies. */
    @Transactional
    public AppelView corrigerAppel(UUID etablissementId, UUID classeId, UUID appelId, CorrigerAppelRequest request) {
        Appel appel = appels.findByIdAndEtablissementId(appelId, etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appel introuvable"));
        if (!appel.getClasseId().equals(classeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appel introuvable pour cette classe");
        }
        for (PresenceSaisie saisie : request.presences()) {
            PresenceEleve presence = presences.findByAppelIdAndEleveId(appelId, saisie.eleveId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "L'élève " + saisie.eleveId() + " n'a pas d'enregistrement pour cet appel"));
            presence.setStatut(saisie.statut());
            presence.setHeureArrivee(saisie.heureArrivee());
            presence.setHeureSortie(saisie.heureSortie());
            presences.save(presence);
        }
        return toView(appel);
    }

    /** CU-SUR-01 : consulter les présences d'une classe pour un jour donné. */
    @Transactional(readOnly = true)
    public AppelView getParClasseEtDate(UUID etablissementId, UUID classeId, LocalDate date) {
        if (!classes.existsByIdAndEtablissementId(classeId, etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable pour cet établissement");
        }
        Appel appel = appels.findByClasseIdAndDateAppel(classeId, date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun appel pour cette classe à cette date"));
        return toView(appel);
    }

    /** Historique des présences d'un élève, pour le personnel de l'établissement. */
    @Transactional(readOnly = true)
    public List<PresenceHistoriqueView> listPourEleve(UUID etablissementId, UUID eleveId, LocalDate from, LocalDate to) {
        if (!inscriptions.existsByEtablissementIdAndEleveId(etablissementId, eleveId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Élève introuvable pour cet établissement");
        }
        return historique(eleveId, from, to);
    }

    /** CU-PAR-05 : un parent/tuteur consulte les présences de son enfant, s'il a un lien validé. */
    @Transactional(readOnly = true)
    public List<PresenceHistoriqueView> listPourParent(UUID parentUtilisateurId, UUID eleveId, LocalDate from, LocalDate to) {
        parentsEleves.findByUtilisateurIdAndEleveId(parentUtilisateurId, eleveId)
                .filter(p -> p.getStatut() == StatutParentEleve.VALIDE && p.isPeutConsulter())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucun lien validé avec cet élève"));
        return historique(eleveId, from, to);
    }

    private List<PresenceHistoriqueView> historique(UUID eleveId, LocalDate from, LocalDate to) {
        List<PresenceEleve> rows = presences.findByEleveId(eleveId);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<UUID, Appel> appelsParId = appels.findAllById(rows.stream().map(PresenceEleve::getAppelId).toList())
                .stream().collect(Collectors.toMap(Appel::getId, a -> a));

        return rows.stream()
                .map(r -> {
                    Appel a = appelsParId.get(r.getAppelId());
                    return a == null ? null : new PresenceHistoriqueView(a.getDateAppel(), r.getStatut(),
                            r.getHeureArrivee(), r.getHeureSortie(), a.getClasseId());
                })
                .filter(v -> v != null)
                .filter(v -> from == null || !v.date().isBefore(from))
                .filter(v -> to == null || !v.date().isAfter(to))
                .sorted(Comparator.comparing(PresenceHistoriqueView::date).reversed())
                .toList();
    }

    private UUID anneeCouranteId(UUID etablissementId) {
        return annees.findByEtablissementIdAndCouranteTrue(etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Aucune année scolaire courante définie pour cet établissement"))
                .getId();
    }

    private AppelView toView(Appel appel) {
        List<PresenceEleve> rows = presences.findByAppelId(appel.getId());
        Map<UUID, Eleve> elevesParId = eleves.findAllById(rows.stream().map(PresenceEleve::getEleveId).toList())
                .stream().collect(Collectors.toMap(Eleve::getId, e -> e));

        List<PresenceEleveView> vues = rows.stream()
                .map(r -> new PresenceEleveView(r.getId(), r.getEleveId(), r.getStatut(), r.getHeureArrivee(),
                        r.getHeureSortie(), EleveView.from(elevesParId.get(r.getEleveId()))))
                .toList();
        return new AppelView(appel.getId(), appel.getClasseId(), appel.getAnneeScolaireId(), appel.getDateAppel(),
                appel.getMatiereId(), vues);
    }
}
