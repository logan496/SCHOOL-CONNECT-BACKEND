package com.schoolconnect.api.school.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Affecte un enseignant à une classe pour une matière, sur une année scolaire (CU-ENS-02). */
@Entity
@Table(name = "affectation_enseignant")
public class AffectationEnseignant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "etablissement_id", nullable = false)
    private UUID etablissementId;

    @Column(name = "annee_scolaire_id", nullable = false)
    private UUID anneeScolaireId;

    @Column(name = "classe_id", nullable = false)
    private UUID classeId;

    @Column(name = "matiere_id", nullable = false)
    private UUID matiereId;

    @Column(name = "enseignant_id", nullable = false)
    private UUID enseignantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getEtablissementId() { return etablissementId; }
    public void setEtablissementId(UUID etablissementId) { this.etablissementId = etablissementId; }
    public UUID getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(UUID anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }
    public UUID getClasseId() { return classeId; }
    public void setClasseId(UUID classeId) { this.classeId = classeId; }
    public UUID getMatiereId() { return matiereId; }
    public void setMatiereId(UUID matiereId) { this.matiereId = matiereId; }
    public UUID getEnseignantId() { return enseignantId; }
    public void setEnseignantId(UUID enseignantId) { this.enseignantId = enseignantId; }
}
