package com.schoolconnect.api.note.service;

import com.schoolconnect.api.eleve.dto.EleveDtos.EleveView;
import com.schoolconnect.api.eleve.model.Eleve;
import com.schoolconnect.api.eleve.model.Inscription;
import com.schoolconnect.api.eleve.model.StatutInscription;
import com.schoolconnect.api.eleve.model.StatutParentEleve;
import com.schoolconnect.api.eleve.repository.EleveRepository;
import com.schoolconnect.api.eleve.repository.InscriptionRepository;
import com.schoolconnect.api.eleve.repository.ParentEleveRepository;
import com.schoolconnect.api.note.dto.NoteDtos.CorrigerNotesRequest;
import com.schoolconnect.api.note.dto.NoteDtos.CreerEvaluationRequest;
import com.schoolconnect.api.note.dto.NoteDtos.EvaluationView;
import com.schoolconnect.api.note.dto.NoteDtos.MatiereNotesView;
import com.schoolconnect.api.note.dto.NoteDtos.NoteHistoriqueView;
import com.schoolconnect.api.note.dto.NoteDtos.NoteView;
import com.schoolconnect.api.note.dto.NoteDtos.SaisieNote;
import com.schoolconnect.api.note.model.Evaluation;
import com.schoolconnect.api.note.model.Note;
import com.schoolconnect.api.note.repository.EvaluationRepository;
import com.schoolconnect.api.note.repository.NoteRepository;
import com.schoolconnect.api.school.model.Periode;
import com.schoolconnect.api.school.repository.AffectationEnseignantRepository;
import com.schoolconnect.api.school.repository.AnneeScolaireRepository;
import com.schoolconnect.api.school.repository.ClasseRepository;
import com.schoolconnect.api.school.repository.MatiereRepository;
import com.schoolconnect.api.school.repository.PeriodeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private static final BigDecimal ECHELLE_BULLETIN = BigDecimal.valueOf(20);

    private final EvaluationRepository evaluations;
    private final NoteRepository notes;
    private final ClasseRepository classes;
    private final MatiereRepository matieres;
    private final PeriodeRepository periodes;
    private final AnneeScolaireRepository annees;
    private final InscriptionRepository inscriptions;
    private final EleveRepository eleves;
    private final AffectationEnseignantRepository affectations;
    private final ParentEleveRepository parentsEleves;

    public NoteService(EvaluationRepository evaluations, NoteRepository notes, ClasseRepository classes,
                       MatiereRepository matieres, PeriodeRepository periodes, AnneeScolaireRepository annees,
                       InscriptionRepository inscriptions, EleveRepository eleves,
                       AffectationEnseignantRepository affectations, ParentEleveRepository parentsEleves) {
        this.evaluations = evaluations;
        this.notes = notes;
        this.classes = classes;
        this.matieres = matieres;
        this.periodes = periodes;
        this.annees = annees;
        this.inscriptions = inscriptions;
        this.eleves = eleves;
        this.affectations = affectations;
        this.parentsEleves = parentsEleves;
    }

    /** CU-ENS-04 : saisir les notes d'une évaluation (créée à la volée). */
    @Transactional
    public EvaluationView creerEvaluation(UUID etablissementId, UUID classeId, UUID matiereId,
                                          CreerEvaluationRequest request, UUID utilisateurId, boolean direction) {
        if (!classes.existsByIdAndEtablissementId(classeId, etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable pour cet établissement");
        }
        if (!matieres.existsByIdAndEtablissementId(matiereId, etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Matière introuvable pour cet établissement");
        }
        UUID anneeId = anneeCouranteId(etablissementId);
        verifierAutorisation(anneeId, classeId, matiereId, utilisateurId, direction);

        Periode periode = periodes.findByIdAndAnneeScolaireId(request.periodeId(), anneeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Période introuvable pour cette année scolaire"));
        if (!periode.isSaisieOuverte()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La saisie des notes est fermée pour cette période");
        }

        List<UUID> rosterIds = roster(classeId, anneeId);
        for (SaisieNote saisie : request.notes()) {
            if (!rosterIds.contains(saisie.eleveId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'élève " + saisie.eleveId() + " n'est pas inscrit dans cette classe");
            }
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setEtablissementId(etablissementId);
        evaluation.setClasseId(classeId);
        evaluation.setMatiereId(matiereId);
        evaluation.setPeriodeId(request.periodeId());
        evaluation.setNom(request.nom().trim());
        evaluation.setCoefficient(request.coefficient());
        evaluation.setBareme(request.bareme());
        evaluation.setDateEvaluation(request.date());
        evaluation.setSaisiPar(utilisateurId);
        evaluation = evaluations.save(evaluation);

        for (SaisieNote saisie : request.notes()) {
            Note note = new Note();
            note.setEvaluationId(evaluation.getId());
            note.setEleveId(saisie.eleveId());
            note.setValeur(saisie.valeur());
            note.setAbsent(saisie.absent());
            note.setModifiePar(utilisateurId);
            notes.save(note);
        }

        return toView(evaluation);
    }

    /** CU-ENS-05 : corrige des notes (y compris celles omises à la création), dans la période ouverte. */
    @Transactional
    public EvaluationView corrigerNotes(UUID etablissementId, UUID classeId, UUID matiereId, UUID evaluationId,
                                        CorrigerNotesRequest request, UUID utilisateurId, boolean direction) {
        Evaluation evaluation = evaluations.findByIdAndEtablissementId(evaluationId, etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Évaluation introuvable"));
        if (!evaluation.getClasseId().equals(classeId) || !evaluation.getMatiereId().equals(matiereId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Évaluation introuvable pour cette classe/matière");
        }
        UUID anneeId = anneeCouranteId(etablissementId);
        verifierAutorisation(anneeId, classeId, matiereId, utilisateurId, direction);

        Periode periode = periodes.findById(evaluation.getPeriodeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Période de cette évaluation introuvable"));
        if (!periode.isSaisieOuverte()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La saisie des notes est fermée pour cette période");
        }

        List<UUID> rosterIds = roster(classeId, anneeId);
        for (SaisieNote saisie : request.notes()) {
            if (!rosterIds.contains(saisie.eleveId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'élève " + saisie.eleveId() + " n'est pas inscrit dans cette classe");
            }
            Note note = this.notes.findByEvaluationIdAndEleveId(evaluationId, saisie.eleveId()).orElseGet(() -> {
                Note n = new Note();
                n.setEvaluationId(evaluationId);
                n.setEleveId(saisie.eleveId());
                return n;
            });
            note.setValeur(saisie.valeur());
            note.setAbsent(saisie.absent());
            note.setModifiePar(utilisateurId);
            this.notes.save(note);
        }

        return toView(evaluation);
    }

    /** Historique des notes d'un élève, pour le personnel de l'établissement. */
    @Transactional(readOnly = true)
    public List<MatiereNotesView> listPourEleve(UUID etablissementId, UUID eleveId, UUID periodeId, UUID matiereId) {
        if (!inscriptions.existsByEtablissementIdAndEleveId(etablissementId, eleveId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Élève introuvable pour cet établissement");
        }
        return historique(eleveId, periodeId, matiereId);
    }

    /** CU-PAR-07 : un parent/tuteur consulte les notes de son enfant par matière et par séquence. */
    @Transactional(readOnly = true)
    public List<MatiereNotesView> listPourParent(UUID parentUtilisateurId, UUID eleveId, UUID periodeId, UUID matiereId) {
        parentsEleves.findByUtilisateurIdAndEleveId(parentUtilisateurId, eleveId)
                .filter(p -> p.getStatut() == StatutParentEleve.VALIDE && p.isPeutConsulter())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucun lien validé avec cet élève"));
        return historique(eleveId, periodeId, matiereId);
    }

    private List<MatiereNotesView> historique(UUID eleveId, UUID periodeId, UUID matiereIdFiltre) {
        List<Note> rows = notes.findByEleveId(eleveId);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<UUID, Evaluation> evaluationsParId = evaluations.findAllById(rows.stream().map(Note::getEvaluationId).toList())
                .stream().collect(Collectors.toMap(Evaluation::getId, e -> e));

        Map<UUID, List<NoteHistoriqueView>> parMatiere = new HashMap<>();
        for (Note n : rows) {
            Evaluation e = evaluationsParId.get(n.getEvaluationId());
            if (e == null || !e.getPeriodeId().equals(periodeId)) {
                continue;
            }
            if (matiereIdFiltre != null && !e.getMatiereId().equals(matiereIdFiltre)) {
                continue;
            }
            parMatiere.computeIfAbsent(e.getMatiereId(), k -> new java.util.ArrayList<>())
                    .add(new NoteHistoriqueView(e.getId(), e.getNom(), e.getDateEvaluation(), e.getCoefficient(),
                            e.getBareme(), n.getValeur(), n.isAbsent()));
        }

        return parMatiere.entrySet().stream()
                .map(entry -> {
                    List<NoteHistoriqueView> liste = entry.getValue().stream()
                            .sorted(Comparator.comparing(NoteHistoriqueView::date)).toList();
                    return new MatiereNotesView(entry.getKey(), moyenne(liste), liste);
                })
                .toList();
    }

    private static BigDecimal moyenne(List<NoteHistoriqueView> notesMatiere) {
        BigDecimal sommePonderee = BigDecimal.ZERO;
        BigDecimal sommeCoefficients = BigDecimal.ZERO;
        for (NoteHistoriqueView n : notesMatiere) {
            if (n.absent() || n.valeur() == null) {
                continue;
            }
            BigDecimal noteSur20 = n.valeur().divide(n.bareme(), 6, RoundingMode.HALF_UP).multiply(ECHELLE_BULLETIN);
            sommePonderee = sommePonderee.add(noteSur20.multiply(n.coefficient()));
            sommeCoefficients = sommeCoefficients.add(n.coefficient());
        }
        if (sommeCoefficients.signum() == 0) {
            return null;
        }
        return sommePonderee.divide(sommeCoefficients, 2, RoundingMode.HALF_UP);
    }

    private void verifierAutorisation(UUID anneeId, UUID classeId, UUID matiereId, UUID utilisateurId, boolean direction) {
        if (direction) {
            return;
        }
        if (!affectations.existsByAnneeScolaireIdAndClasseIdAndMatiereIdAndEnseignantId(anneeId, classeId, matiereId, utilisateurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas affecté à cette classe pour cette matière");
        }
    }

    private List<UUID> roster(UUID classeId, UUID anneeId) {
        return inscriptions.findByClasseIdAndAnneeScolaireIdAndStatut(classeId, anneeId, StatutInscription.VALIDE)
                .stream().map(Inscription::getEleveId).toList();
    }

    private UUID anneeCouranteId(UUID etablissementId) {
        return annees.findByEtablissementIdAndCouranteTrue(etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Aucune année scolaire courante définie pour cet établissement"))
                .getId();
    }

    private EvaluationView toView(Evaluation evaluation) {
        List<Note> rows = notes.findByEvaluationId(evaluation.getId());
        Map<UUID, Eleve> elevesParId = eleves.findAllById(rows.stream().map(Note::getEleveId).toList())
                .stream().collect(Collectors.toMap(Eleve::getId, e -> e));

        List<NoteView> vues = rows.stream()
                .map(n -> new NoteView(n.getId(), n.getEleveId(), n.getValeur(), n.isAbsent(),
                        EleveView.from(elevesParId.get(n.getEleveId()))))
                .toList();
        return new EvaluationView(evaluation.getId(), evaluation.getClasseId(), evaluation.getMatiereId(),
                evaluation.getPeriodeId(), evaluation.getNom(), evaluation.getCoefficient(), evaluation.getBareme(),
                evaluation.getDateEvaluation(), vues);
    }
}
