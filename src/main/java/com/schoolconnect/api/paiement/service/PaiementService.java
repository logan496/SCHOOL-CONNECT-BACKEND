package com.schoolconnect.api.paiement.service;

import com.schoolconnect.api.eleve.dto.EleveDtos.EleveView;
import com.schoolconnect.api.eleve.model.Eleve;
import com.schoolconnect.api.eleve.model.Inscription;
import com.schoolconnect.api.eleve.model.StatutInscription;
import com.schoolconnect.api.eleve.model.StatutParentEleve;
import com.schoolconnect.api.eleve.repository.EleveRepository;
import com.schoolconnect.api.eleve.repository.InscriptionRepository;
import com.schoolconnect.api.eleve.repository.ParentEleveRepository;
import com.schoolconnect.api.notification.model.TypeNotification;
import com.schoolconnect.api.notification.service.NotificationService;
import com.schoolconnect.api.paiement.dto.PaiementDtos.AnnulerPaiementRequest;
import com.schoolconnect.api.paiement.dto.PaiementDtos.CreerFraisScolaireRequest;
import com.schoolconnect.api.paiement.dto.PaiementDtos.EnregistrerPaiementRequest;
import com.schoolconnect.api.paiement.dto.PaiementDtos.FraisScolaireView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.ImpayeView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.PaiementView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.RapportCaisseLigne;
import com.schoolconnect.api.paiement.dto.PaiementDtos.RapportCaisseView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.SituationFinanciereView;
import com.schoolconnect.api.paiement.model.FraisScolaire;
import com.schoolconnect.api.paiement.model.ModePaiement;
import com.schoolconnect.api.paiement.model.Paiement;
import com.schoolconnect.api.paiement.model.StatutPaiement;
import com.schoolconnect.api.paiement.repository.FraisScolaireRepository;
import com.schoolconnect.api.paiement.repository.PaiementRepository;
import com.schoolconnect.api.school.model.Classe;
import com.schoolconnect.api.school.repository.AnneeScolaireRepository;
import com.schoolconnect.api.school.repository.ClasseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaiementService {

    private static final String RECU_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int RECU_LENGTH = 10;
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FraisScolaireRepository fraisScolaires;
    private final PaiementRepository paiements;
    private final AnneeScolaireRepository annees;
    private final ClasseRepository classes;
    private final InscriptionRepository inscriptions;
    private final EleveRepository eleves;
    private final ParentEleveRepository parentsEleves;
    private final NotificationService notifications;
    private final SecureRandom random = new SecureRandom();

    public PaiementService(FraisScolaireRepository fraisScolaires, PaiementRepository paiements,
                           AnneeScolaireRepository annees, ClasseRepository classes,
                           InscriptionRepository inscriptions, EleveRepository eleves,
                           ParentEleveRepository parentsEleves, NotificationService notifications) {
        this.fraisScolaires = fraisScolaires;
        this.paiements = paiements;
        this.annees = annees;
        this.classes = classes;
        this.inscriptions = inscriptions;
        this.eleves = eleves;
        this.parentsEleves = parentsEleves;
        this.notifications = notifications;
    }

    /** CU-DIR-04 : définir la grille tarifaire (une ligne = un montant dû, pour un niveau, à une échéance). */
    @Transactional
    public FraisScolaireView creerFraisScolaire(UUID etablissementId, UUID anneeScolaireId, CreerFraisScolaireRequest request) {
        if (!annees.existsByIdAndEtablissementId(anneeScolaireId, etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Année scolaire introuvable pour cet établissement");
        }
        FraisScolaire f = new FraisScolaire();
        f.setEtablissementId(etablissementId);
        f.setAnneeScolaireId(anneeScolaireId);
        f.setNiveau(request.niveau().trim());
        f.setLibelle(request.libelle().trim());
        f.setMontant(request.montant());
        f.setDateEcheance(request.dateEcheance());
        return toView(fraisScolaires.save(f));
    }

    @Transactional(readOnly = true)
    public List<FraisScolaireView> listFraisScolaire(UUID etablissementId, UUID anneeScolaireId) {
        if (!annees.existsByIdAndEtablissementId(anneeScolaireId, etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Année scolaire introuvable pour cet établissement");
        }
        return fraisScolaires.findByEtablissementIdAndAnneeScolaireId(etablissementId, anneeScolaireId).stream()
                .map(PaiementService::toView).toList();
    }

    /** CU-ECO-02 : enregistrer un paiement déjà effectué, avec reçu numérique (CU-ECO-03). */
    @Transactional
    public PaiementView enregistrerPaiement(UUID etablissementId, UUID eleveId, EnregistrerPaiementRequest request, UUID enregistrePar) {
        UUID anneeId = anneeCouranteId(etablissementId);
        if (!inscriptions.existsByEtablissementIdAndEleveId(etablissementId, eleveId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Élève introuvable pour cet établissement");
        }

        Paiement p = new Paiement();
        p.setEtablissementId(etablissementId);
        p.setEleveId(eleveId);
        p.setAnneeScolaireId(anneeId);
        p.setMontant(request.montant());
        p.setMode(request.mode());
        p.setReference(blankToNull(request.reference()));
        p.setDatePaiement(request.date());
        p.setNumeroRecu(genererNumeroRecuUnique());
        p.setStatut(StatutPaiement.VALIDE);
        p.setEnregistrePar(enregistrePar);
        p = paiements.save(p);
        notifications.notifierParents(etablissementId, eleveId, TypeNotification.PAIEMENT_ENREGISTRE,
                "Paiement enregistré pour " + nomEleve(eleveId),
                "Un paiement de " + montant(p.getMontant()) + " du " + p.getDatePaiement().format(FORMAT_DATE)
                        + " a été enregistré. Reçu n° " + p.getNumeroRecu() + ".");
        return toView(p);
    }

    /** CU-ECO-07 : annuler un paiement, avec justification et traçabilité (pas de suppression). */
    @Transactional
    public PaiementView annulerPaiement(UUID etablissementId, UUID eleveId, UUID paiementId, AnnulerPaiementRequest request, UUID annulePar) {
        Paiement p = paiements.findByIdAndEtablissementId(paiementId, etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paiement introuvable"));
        if (!p.getEleveId().equals(eleveId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paiement introuvable pour cet élève");
        }
        if (p.getStatut() != StatutPaiement.VALIDE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce paiement est déjà annulé");
        }
        p.setStatut(StatutPaiement.ANNULE);
        p.setMotifAnnulation(request.motif());
        p.setAnnulePar(annulePar);
        p.setAnnuleLe(Instant.now());
        p = paiements.save(p);
        notifications.notifierParents(etablissementId, eleveId, TypeNotification.PAIEMENT_ANNULE,
                "Paiement annulé pour " + nomEleve(eleveId),
                "Le paiement de " + montant(p.getMontant()) + " (reçu n° " + p.getNumeroRecu()
                        + ") a été annulé par l'établissement. Motif : " + p.getMotifAnnulation());
        return toView(p);
    }

    /** CU-ECO-01 : situation financière d'un élève, pour le personnel de l'établissement. */
    @Transactional(readOnly = true)
    public SituationFinanciereView situationFinanciere(UUID etablissementId, UUID eleveId) {
        if (!inscriptions.existsByEtablissementIdAndEleveId(etablissementId, eleveId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Élève introuvable pour cet établissement");
        }
        return calculerSituation(etablissementId, eleveId);
    }

    /** CU-PAR-10/12 : un parent/tuteur consulte la situation financière et l'historique des reçus de son enfant. */
    @Transactional(readOnly = true)
    public SituationFinanciereView situationFinancierePourParent(UUID parentUtilisateurId, UUID eleveId) {
        parentsEleves.findByUtilisateurIdAndEleveId(parentUtilisateurId, eleveId)
                .filter(p -> p.getStatut() == StatutParentEleve.VALIDE && p.isPeutConsulter())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucun lien validé avec cet élève"));
        UUID etablissementId = inscriptions.findFirstByEleveIdAndStatutOrderByDateInscriptionDesc(eleveId, StatutInscription.VALIDE)
                .map(Inscription::getEtablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune inscription active pour cet élève"));
        return calculerSituation(etablissementId, eleveId);
    }

    /** CU-ECO-06 / CU-DIR-07 : élèves en solde positif (dû non couvert) pour l'année courante. */
    @Transactional(readOnly = true)
    public List<ImpayeView> listImpayes(UUID etablissementId) {
        UUID anneeId = anneeCouranteId(etablissementId);
        return inscriptions.findByEtablissementIdAndAnneeScolaireIdOrderByMatricule(etablissementId, anneeId).stream()
                .map(i -> calculerSituation(etablissementId, i.getEleveId()))
                .filter(s -> s.solde().signum() > 0)
                .sorted(Comparator.comparing(SituationFinanciereView::solde).reversed())
                .map(s -> new ImpayeView(s.eleveId(), s.eleve(), s.totalDu(), s.totalPaye(), s.solde()))
                .toList();
    }

    /**
     * Élèves dont le montant arrivé à échéance au {@code date} n'est pas couvert par leurs paiements.
     * Contrairement à {@link #listImpayes}, les frais dont l'échéance n'est pas encore passée sont
     * ignorés : c'est la base des relances (CU-ECO-06, CU-SYS-03). {@code totalDu} = dû échu.
     */
    @Transactional(readOnly = true)
    public List<ImpayeView> listImpayesEchus(UUID etablissementId, LocalDate date) {
        UUID anneeId = anneeCouranteId(etablissementId);
        return inscriptions.findByEtablissementIdAndAnneeScolaireIdOrderByMatricule(etablissementId, anneeId).stream()
                .map(i -> calculerSituation(etablissementId, i.getEleveId()))
                .map(s -> {
                    BigDecimal duEchu = s.fraisApplicables().stream()
                            .filter(f -> !f.dateEcheance().isAfter(date))
                            .map(FraisScolaireView::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ImpayeView(s.eleveId(), s.eleve(), duEchu, s.totalPaye(), duEchu.subtract(s.totalPaye()));
                })
                .filter(i -> i.solde().signum() > 0)
                .sorted(Comparator.comparing(ImpayeView::solde).reversed())
                .toList();
    }

    /** Formate un montant pour les messages aux parents (ex. « 25 000 FCFA »). */
    public static String montant(BigDecimal montant) {
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0.##", symboles).format(montant) + " FCFA";
    }

    private String nomEleve(UUID eleveId) {
        return eleves.findById(eleveId).map(e -> e.getPrenom() + " " + e.getNom()).orElse("votre enfant");
    }

    /** CU-ECO-08 : rapport de caisse pour un jour donné, par mode de paiement. */
    @Transactional(readOnly = true)
    public RapportCaisseView rapportCaisse(UUID etablissementId, LocalDate date) {
        List<Paiement> jour = paiements.findByEtablissementIdAndDatePaiement(etablissementId, date).stream()
                .filter(p -> p.getStatut() == StatutPaiement.VALIDE)
                .toList();
        Map<ModePaiement, List<Paiement>> parMode = jour.stream().collect(Collectors.groupingBy(Paiement::getMode));
        List<RapportCaisseLigne> lignes = parMode.entrySet().stream()
                .map(e -> new RapportCaisseLigne(e.getKey(),
                        e.getValue().stream().map(Paiement::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add),
                        e.getValue().size()))
                .sorted(Comparator.comparing(RapportCaisseLigne::mode))
                .toList();
        BigDecimal total = jour.stream().map(Paiement::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RapportCaisseView(date, total, lignes);
    }

    private SituationFinanciereView calculerSituation(UUID etablissementId, UUID eleveId) {
        UUID anneeId = anneeCouranteId(etablissementId);
        Eleve eleve = eleves.findById(eleveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Élève introuvable"));

        List<FraisScolaireView> fraisApplicables = inscriptions
                .findByEleveIdAndEtablissementIdAndAnneeScolaireId(eleveId, etablissementId, anneeId)
                .map(Inscription::getClasseId)
                .flatMap(classeId -> classeId == null ? java.util.Optional.<Classe>empty() : classes.findById(classeId))
                .map(classe -> fraisScolaires.findByEtablissementIdAndAnneeScolaireIdAndNiveau(etablissementId, anneeId, classe.getNiveau()))
                .orElse(List.of())
                .stream().map(PaiementService::toView).toList();

        BigDecimal totalDu = fraisApplicables.stream().map(FraisScolaireView::montant).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PaiementView> paiementsEleve = paiements.findByEleveIdAndAnneeScolaireIdOrderByDatePaiementDesc(eleveId, anneeId)
                .stream().map(PaiementService::toView).toList();
        BigDecimal totalPaye = paiementsEleve.stream()
                .filter(p -> p.statut() == StatutPaiement.VALIDE)
                .map(PaiementView::montant).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SituationFinanciereView(eleveId, EleveView.from(eleve), totalDu, totalPaye,
                totalDu.subtract(totalPaye), fraisApplicables, paiementsEleve);
    }

    private UUID anneeCouranteId(UUID etablissementId) {
        return annees.findByEtablissementIdAndCouranteTrue(etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Aucune année scolaire courante définie pour cet établissement"))
                .getId();
    }

    private String genererNumeroRecuUnique() {
        String numero;
        do {
            StringBuilder sb = new StringBuilder(RECU_LENGTH);
            for (int i = 0; i < RECU_LENGTH; i++) {
                sb.append(RECU_ALPHABET.charAt(random.nextInt(RECU_ALPHABET.length())));
            }
            numero = "R-" + sb;
        } while (paiements.existsByNumeroRecu(numero));
        return numero;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static FraisScolaireView toView(FraisScolaire f) {
        return new FraisScolaireView(f.getId(), f.getAnneeScolaireId(), f.getNiveau(), f.getLibelle(), f.getMontant(), f.getDateEcheance());
    }

    private static PaiementView toView(Paiement p) {
        return new PaiementView(p.getId(), p.getEleveId(), p.getMontant(), p.getMode(), p.getReference(),
                p.getDatePaiement(), p.getNumeroRecu(), p.getStatut());
    }
}
