package com.schoolconnect.api.eleve.service;

import com.schoolconnect.api.eleve.dto.EleveDtos.AffecterClasseRequest;
import com.schoolconnect.api.eleve.dto.EleveDtos.CodeLiaisonView;
import com.schoolconnect.api.eleve.dto.EleveDtos.EnregistrerTuteurRequest;
import com.schoolconnect.api.eleve.dto.EleveDtos.InscriptionView;
import com.schoolconnect.api.eleve.dto.EleveDtos.InscrireEleveRequest;
import com.schoolconnect.api.eleve.dto.EleveDtos.ParentEleveView;
import com.schoolconnect.api.eleve.dto.EleveDtos.TuteurLiaisonResult;
import com.schoolconnect.api.eleve.model.CodeLiaison;
import com.schoolconnect.api.eleve.model.Eleve;
import com.schoolconnect.api.eleve.model.Inscription;
import com.schoolconnect.api.eleve.model.ParentEleve;
import com.schoolconnect.api.eleve.model.StatutInscription;
import com.schoolconnect.api.eleve.model.StatutParentEleve;
import com.schoolconnect.api.eleve.repository.CodeLiaisonRepository;
import com.schoolconnect.api.eleve.repository.EleveRepository;
import com.schoolconnect.api.eleve.repository.InscriptionRepository;
import com.schoolconnect.api.eleve.repository.ParentEleveRepository;
import com.schoolconnect.api.identity.model.Utilisateur;
import com.schoolconnect.api.identity.repository.UtilisateurRepository;
import com.schoolconnect.api.identity.service.PhoneNumbers;
import com.schoolconnect.api.school.repository.AnneeScolaireRepository;
import com.schoolconnect.api.school.repository.ClasseRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EleveService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sans caractères ambigus (0/O, 1/I)
    private static final int CODE_LENGTH = 8;
    private static final int CODE_VALIDITE_JOURS = 30;

    private final EleveRepository eleves;
    private final InscriptionRepository inscriptions;
    private final ParentEleveRepository parentsEleves;
    private final CodeLiaisonRepository codesLiaison;
    private final UtilisateurRepository utilisateurs;
    private final AnneeScolaireRepository annees;
    private final ClasseRepository classes;
    private final SecureRandom random = new SecureRandom();

    public EleveService(EleveRepository eleves, InscriptionRepository inscriptions,
                        ParentEleveRepository parentsEleves, CodeLiaisonRepository codesLiaison,
                        UtilisateurRepository utilisateurs, AnneeScolaireRepository annees,
                        ClasseRepository classes) {
        this.eleves = eleves;
        this.inscriptions = inscriptions;
        this.parentsEleves = parentsEleves;
        this.codesLiaison = codesLiaison;
        this.utilisateurs = utilisateurs;
        this.annees = annees;
        this.classes = classes;
    }

    /** CU-SEC-01 : inscrire un nouvel élève et créer son dossier. */
    @Transactional
    public InscriptionView inscrire(UUID etablissementId, InscrireEleveRequest request) {
        if (!annees.existsByIdAndEtablissementId(request.anneeScolaireId(), etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Année scolaire introuvable pour cet établissement");
        }
        if (request.classeId() != null && !classes.existsByIdAndEtablissementId(request.classeId(), etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable pour cet établissement");
        }
        String matricule = request.matricule().trim();
        if (inscriptions.existsByEtablissementIdAndMatricule(etablissementId, matricule)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce matricule est déjà utilisé dans cet établissement");
        }

        Eleve eleve = new Eleve();
        eleve.setNom(request.nom().trim());
        eleve.setPrenom(request.prenom().trim());
        eleve.setSexe(request.sexe());
        eleve.setDateNaissance(request.dateNaissance());
        eleve.setLieuNaissance(blankToNull(request.lieuNaissance()));
        eleve.setNationalite(blankToNull(request.nationalite()));
        eleve.setMatriculeNational(blankToNull(request.matriculeNational()));
        eleve = eleves.save(eleve);

        Inscription inscription = new Inscription();
        inscription.setEtablissementId(etablissementId);
        inscription.setEleveId(eleve.getId());
        inscription.setAnneeScolaireId(request.anneeScolaireId());
        inscription.setClasseId(request.classeId());
        inscription.setMatricule(matricule);
        inscription.setType(request.type());
        inscription.setRedoublant(request.redoublant());
        inscription.setEtablissementOrigine(blankToNull(request.etablissementOrigine()));
        inscription.setStatut(StatutInscription.VALIDE);
        inscription = inscriptions.save(inscription);

        return InscriptionView.from(inscription, eleve);
    }

    @Transactional(readOnly = true)
    public List<InscriptionView> list(UUID etablissementId, UUID anneeScolaireId) {
        return inscriptions.findByEtablissementIdAndAnneeScolaireIdOrderByMatricule(etablissementId, anneeScolaireId)
                .stream()
                .map(i -> InscriptionView.from(i, eleves.findById(i.getEleveId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Élève introuvable"))))
                .toList();
    }

    /** CU-SEC-03 : affecter ou transférer un élève dans une classe (année scolaire courante). */
    @Transactional
    public InscriptionView affecterClasse(UUID etablissementId, UUID eleveId, AffecterClasseRequest request) {
        if (!classes.existsByIdAndEtablissementId(request.classeId(), etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable pour cet établissement");
        }
        Inscription inscription = inscriptionCourante(etablissementId, eleveId);
        inscription.setClasseId(request.classeId());
        Eleve eleve = eleves.findById(eleveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Élève introuvable"));
        return InscriptionView.from(inscriptions.save(inscription), eleve);
    }

    /** CU-SEC-04 : rattache directement un tuteur déjà titulaire d'un compte, ou génère un code de liaison sinon. */
    @Transactional
    public TuteurLiaisonResult enregistrerTuteur(UUID etablissementId, UUID eleveId, UUID secretaireId,
                                                  EnregistrerTuteurRequest request) {
        requireEleveDansEtablissement(etablissementId, eleveId);

        String telephone = PhoneNumbers.normalize(request.telephone());
        String email = blankToNull(request.email() == null ? null : request.email().trim().toLowerCase(Locale.ROOT));
        if (telephone == null && email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un numéro de téléphone ou un email du tuteur est requis");
        }

        Optional<Utilisateur> compteExistant = telephone != null
                ? utilisateurs.findByTelephone(telephone)
                : utilisateurs.findByEmail(email);

        if (compteExistant.isPresent()) {
            Utilisateur tuteur = compteExistant.get();
            if (parentsEleves.findByUtilisateurIdAndEleveId(tuteur.getId(), eleveId).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce tuteur est déjà lié à cet élève");
            }
            ParentEleve lien = new ParentEleve();
            lien.setUtilisateurId(tuteur.getId());
            lien.setEleveId(eleveId);
            lien.setLien(request.lien());
            lien.setPeutPayer(request.peutPayer());
            lien.setPeutJustifier(request.peutJustifier());
            lien.setStatut(StatutParentEleve.VALIDE);
            lien.setValidePar(secretaireId);
            lien.setValideLe(Instant.now());
            return new TuteurLiaisonResult(ParentEleveView.from(parentsEleves.save(lien)), null);
        }

        CodeLiaison code = new CodeLiaison();
        code.setEleveId(eleveId);
        code.setEtablissementId(etablissementId);
        code.setCode(genererCodeUnique());
        code.setLien(request.lien());
        code.setCreePar(secretaireId);
        code.setExpireLe(Instant.now().plus(CODE_VALIDITE_JOURS, ChronoUnit.DAYS));
        return new TuteurLiaisonResult(null, CodeLiaisonView.from(codesLiaison.save(code)));
    }

    @Transactional(readOnly = true)
    public List<ParentEleveView> listTuteurs(UUID etablissementId, UUID eleveId) {
        requireEleveDansEtablissement(etablissementId, eleveId);
        return parentsEleves.findByEleveId(eleveId).stream().map(ParentEleveView::from).toList();
    }

    /** CU-SEC-05 : valide une demande de liaison parent-enfant en attente. */
    @Transactional
    public ParentEleveView validerLiaison(UUID etablissementId, UUID eleveId, UUID parentEleveId, UUID secretaireId) {
        return changerStatutLiaison(etablissementId, eleveId, parentEleveId, secretaireId, StatutParentEleve.VALIDE);
    }

    /** CU-SEC-05 : rejette une demande de liaison parent-enfant en attente. */
    @Transactional
    public ParentEleveView rejeterLiaison(UUID etablissementId, UUID eleveId, UUID parentEleveId, UUID secretaireId) {
        return changerStatutLiaison(etablissementId, eleveId, parentEleveId, secretaireId, StatutParentEleve.REVOQUE);
    }

    private ParentEleveView changerStatutLiaison(UUID etablissementId, UUID eleveId, UUID parentEleveId,
                                                  UUID secretaireId, StatutParentEleve nouveauStatut) {
        requireEleveDansEtablissement(etablissementId, eleveId);
        ParentEleve lien = parentsEleves.findByIdAndEleveId(parentEleveId, eleveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande de liaison introuvable"));
        if (lien.getStatut() != StatutParentEleve.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette demande a déjà été traitée");
        }
        lien.setStatut(nouveauStatut);
        lien.setValidePar(secretaireId);
        lien.setValideLe(Instant.now());
        return ParentEleveView.from(parentsEleves.save(lien));
    }

    private Inscription inscriptionCourante(UUID etablissementId, UUID eleveId) {
        UUID anneeId = annees.findByEtablissementIdAndCouranteTrue(etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Aucune année scolaire courante définie pour cet établissement"))
                .getId();
        return inscriptions.findByEleveIdAndEtablissementIdAndAnneeScolaireId(eleveId, etablissementId, anneeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune inscription de cet élève pour l'année scolaire courante"));
    }

    private void requireEleveDansEtablissement(UUID etablissementId, UUID eleveId) {
        if (!inscriptions.existsByEtablissementIdAndEleveId(etablissementId, eleveId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Élève introuvable pour cet établissement");
        }
    }

    private String genererCodeUnique() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (codesLiaison.existsByCode(code));
        return code;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
