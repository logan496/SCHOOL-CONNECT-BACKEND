package com.schoolconnect.api.school;

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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "annee_scolaire")
public class AnneeScolaire {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "etablissement_id", nullable = false)
    private UUID etablissementId;

    @Column(nullable = false, length = 20)
    private String libelle;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private boolean courante = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutAnnee statut = StatutAnnee.PREPARATION;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getEtablissementId() { return etablissementId; }
    public void setEtablissementId(UUID etablissementId) { this.etablissementId = etablissementId; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public boolean isCourante() { return courante; }
    public void setCourante(boolean courante) { this.courante = courante; }
    public StatutAnnee getStatut() { return statut; }
    public void setStatut(StatutAnnee statut) { this.statut = statut; }
}
