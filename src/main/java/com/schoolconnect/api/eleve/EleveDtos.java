package com.schoolconnect.api.eleve;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class EleveDtos {

    private EleveDtos() {
    }

    public record InscrireEleveRequest(
            @NotBlank @Size(max = 100) String nom,
            @NotBlank @Size(max = 100) String prenom,
            @NotNull @Pattern(regexp = "[MF]") String sexe,
            @NotNull LocalDate dateNaissance,
            @Size(max = 100) String lieuNaissance,
            @Size(max = 50) String nationalite,
            @Size(max = 30) String matriculeNational,
            @NotNull UUID anneeScolaireId,
            UUID classeId,
            @NotBlank @Size(max = 30) String matricule,
            @NotNull TypeInscription type,
            boolean redoublant,
            @Size(max = 200) String etablissementOrigine) {
    }

    public record AffecterClasseRequest(@NotNull UUID classeId) {
    }

    public record EnregistrerTuteurRequest(
            @NotBlank @Size(max = 100) String nom,
            @NotBlank @Size(max = 100) String prenom,
            @Size(max = 20) String telephone,
            @Email @Size(max = 150) String email,
            @NotNull LienParente lien,
            boolean peutPayer,
            boolean peutJustifier) {
    }

    public record EleveView(
            UUID id, String matriculeNational, String nom, String prenom, String sexe,
            LocalDate dateNaissance, String lieuNaissance, String nationalite) {

        public static EleveView from(Eleve e) {
            return new EleveView(e.getId(), e.getMatriculeNational(), e.getNom(), e.getPrenom(), e.getSexe(),
                    e.getDateNaissance(), e.getLieuNaissance(), e.getNationalite());
        }
    }

    public record InscriptionView(
            UUID id, UUID eleveId, UUID anneeScolaireId, UUID classeId, String matricule,
            TypeInscription type, boolean redoublant, StatutInscription statut, LocalDate dateInscription,
            EleveView eleve) {

        public static InscriptionView from(Inscription i, Eleve e) {
            return new InscriptionView(i.getId(), i.getEleveId(), i.getAnneeScolaireId(), i.getClasseId(),
                    i.getMatricule(), i.getType(), i.isRedoublant(), i.getStatut(), i.getDateInscription(),
                    EleveView.from(e));
        }
    }

    /** Résultat de CU-SEC-04 : soit un lien direct (le tuteur avait déjà un compte), soit un code à lui remettre. */
    public record TuteurLiaisonResult(ParentEleveView lienDirect, CodeLiaisonView code) {
    }

    public record ParentEleveView(
            UUID id, UUID utilisateurId, UUID eleveId, LienParente lien, boolean peutConsulter,
            boolean peutPayer, boolean peutJustifier, StatutParentEleve statut) {

        public static ParentEleveView from(ParentEleve p) {
            return new ParentEleveView(p.getId(), p.getUtilisateurId(), p.getEleveId(), p.getLien(),
                    p.isPeutConsulter(), p.isPeutPayer(), p.isPeutJustifier(), p.getStatut());
        }
    }

    public record CodeLiaisonView(UUID id, UUID eleveId, String code, LienParente lien, Instant expireLe) {

        public static CodeLiaisonView from(CodeLiaison c) {
            return new CodeLiaisonView(c.getId(), c.getEleveId(), c.getCode(), c.getLien(), c.getExpireLe());
        }
    }
}
