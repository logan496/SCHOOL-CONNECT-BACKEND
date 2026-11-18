package com.schoolconnect.api.eleve;

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

/** Code remis au tuteur pour rattacher son futur compte à l'élève (CU-SEC-04). */
@Entity
@Table(name = "code_liaison")
public class CodeLiaison {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "eleve_id", nullable = false)
    private UUID eleveId;

    @Column(name = "etablissement_id", nullable = false)
    private UUID etablissementId;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LienParente lien;

    @Column(name = "cree_par", nullable = false)
    private UUID creePar;

    @Column(name = "expire_le", nullable = false)
    private Instant expireLe;

    @Column(nullable = false)
    private boolean utilise = false;

    @Column(name = "utilise_par")
    private UUID utiliseParId;

    @Column(name = "utilise_le")
    private Instant utiliseLe;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getEleveId() { return eleveId; }
    public void setEleveId(UUID eleveId) { this.eleveId = eleveId; }
    public UUID getEtablissementId() { return etablissementId; }
    public void setEtablissementId(UUID etablissementId) { this.etablissementId = etablissementId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public LienParente getLien() { return lien; }
    public void setLien(LienParente lien) { this.lien = lien; }
    public UUID getCreePar() { return creePar; }
    public void setCreePar(UUID creePar) { this.creePar = creePar; }
    public Instant getExpireLe() { return expireLe; }
    public void setExpireLe(Instant expireLe) { this.expireLe = expireLe; }
    public boolean isUtilise() { return utilise; }
    public void setUtilise(boolean utilise) { this.utilise = utilise; }
    public UUID getUtiliseParId() { return utiliseParId; }
    public void setUtiliseParId(UUID utiliseParId) { this.utiliseParId = utiliseParId; }
    public Instant getUtiliseLe() { return utiliseLe; }
    public void setUtiliseLe(Instant utiliseLe) { this.utiliseLe = utiliseLe; }
}
