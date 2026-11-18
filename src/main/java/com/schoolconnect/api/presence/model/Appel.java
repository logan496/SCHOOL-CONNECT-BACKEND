package com.schoolconnect.api.presence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Un appel = une classe, un jour. Chaque élève inscrit y a exactement une ligne {@link PresenceEleve}. */
@Entity
@Table(name = "appel")
public class Appel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "etablissement_id", nullable = false)
    private UUID etablissementId;

    @Column(name = "classe_id", nullable = false)
    private UUID classeId;

    @Column(name = "annee_scolaire_id", nullable = false)
    private UUID anneeScolaireId;

    @Column(name = "date_appel", nullable = false)
    private LocalDate dateAppel;

    @Column(name = "matiere_id")
    private UUID matiereId;

    @Column(name = "saisi_par", nullable = false)
    private UUID saisiPar;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getEtablissementId() { return etablissementId; }
    public void setEtablissementId(UUID etablissementId) { this.etablissementId = etablissementId; }
    public UUID getClasseId() { return classeId; }
    public void setClasseId(UUID classeId) { this.classeId = classeId; }
    public UUID getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(UUID anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }
    public LocalDate getDateAppel() { return dateAppel; }
    public void setDateAppel(LocalDate dateAppel) { this.dateAppel = dateAppel; }
    public UUID getMatiereId() { return matiereId; }
    public void setMatiereId(UUID matiereId) { this.matiereId = matiereId; }
    public UUID getSaisiPar() { return saisiPar; }
    public void setSaisiPar(UUID saisiPar) { this.saisiPar = saisiPar; }
}
