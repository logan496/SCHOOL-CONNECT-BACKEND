package com.schoolconnect.api.eleve.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Lien entre un compte parent/tuteur et un enfant, avec droits distincts (voir, payer, justifier). */
@Entity
@Table(name = "parent_eleve")
public class ParentEleve {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "eleve_id", nullable = false)
    private UUID eleveId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LienParente lien;

    @Column(nullable = false)
    private boolean principal = false;

    @Column(name = "peut_consulter", nullable = false)
    private boolean peutConsulter = true;

    @Column(name = "peut_payer", nullable = false)
    private boolean peutPayer = false;

    @Column(name = "peut_justifier", nullable = false)
    private boolean peutJustifier = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutParentEleve statut = StatutParentEleve.EN_ATTENTE;

    @Column(name = "valide_par")
    private UUID validePar;

    @Column(name = "valide_le")
    private Instant valideLe;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(UUID utilisateurId) { this.utilisateurId = utilisateurId; }
    public UUID getEleveId() { return eleveId; }
    public void setEleveId(UUID eleveId) { this.eleveId = eleveId; }
    public LienParente getLien() { return lien; }
    public void setLien(LienParente lien) { this.lien = lien; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
    public boolean isPeutConsulter() { return peutConsulter; }
    public void setPeutConsulter(boolean peutConsulter) { this.peutConsulter = peutConsulter; }
    public boolean isPeutPayer() { return peutPayer; }
    public void setPeutPayer(boolean peutPayer) { this.peutPayer = peutPayer; }
    public boolean isPeutJustifier() { return peutJustifier; }
    public void setPeutJustifier(boolean peutJustifier) { this.peutJustifier = peutJustifier; }
    public StatutParentEleve getStatut() { return statut; }
    public void setStatut(StatutParentEleve statut) { this.statut = statut; }
    public UUID getValidePar() { return validePar; }
    public void setValidePar(UUID validePar) { this.validePar = validePar; }
    public Instant getValideLe() { return valideLe; }
    public void setValideLe(Instant valideLe) { this.valideLe = valideLe; }
}
