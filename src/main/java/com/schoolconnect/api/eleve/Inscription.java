package com.schoolconnect.api.eleve;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "inscription")
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "etablissement_id", nullable = false)
    private UUID etablissementId;

    @Column(name = "eleve_id", nullable = false)
    private UUID eleveId;

    @Column(name = "annee_scolaire_id", nullable = false)
    private UUID anneeScolaireId;

    @Column(name = "classe_id")
    private UUID classeId;

    @Column(nullable = false, length = 30)
    private String matricule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeInscription type;

    @Column(nullable = false)
    private boolean redoublant = false;

    @Column(name = "etablissement_origine", length = 200)
    private String etablissementOrigine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutInscription statut;

    @Column(name = "date_inscription", nullable = false)
    private LocalDate dateInscription;

    @Column(name = "date_sortie")
    private LocalDate dateSortie;

    @Column(name = "motif_sortie")
    private String motifSortie;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (dateInscription == null) {
            dateInscription = LocalDate.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getEtablissementId() { return etablissementId; }
    public void setEtablissementId(UUID etablissementId) { this.etablissementId = etablissementId; }
    public UUID getEleveId() { return eleveId; }
    public void setEleveId(UUID eleveId) { this.eleveId = eleveId; }
    public UUID getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(UUID anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }
    public UUID getClasseId() { return classeId; }
    public void setClasseId(UUID classeId) { this.classeId = classeId; }
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public TypeInscription getType() { return type; }
    public void setType(TypeInscription type) { this.type = type; }
    public boolean isRedoublant() { return redoublant; }
    public void setRedoublant(boolean redoublant) { this.redoublant = redoublant; }
    public String getEtablissementOrigine() { return etablissementOrigine; }
    public void setEtablissementOrigine(String etablissementOrigine) { this.etablissementOrigine = etablissementOrigine; }
    public StatutInscription getStatut() { return statut; }
    public void setStatut(StatutInscription statut) { this.statut = statut; }
    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }
    public LocalDate getDateSortie() { return dateSortie; }
    public void setDateSortie(LocalDate dateSortie) { this.dateSortie = dateSortie; }
    public String getMotifSortie() { return motifSortie; }
    public void setMotifSortie(String motifSortie) { this.motifSortie = motifSortie; }
}
