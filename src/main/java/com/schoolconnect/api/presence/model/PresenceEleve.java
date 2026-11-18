package com.schoolconnect.api.presence.model;

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
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "presence_eleve")
public class PresenceEleve {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "appel_id", nullable = false)
    private UUID appelId;

    @Column(name = "eleve_id", nullable = false)
    private UUID eleveId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPresence statut;

    @Column(name = "heure_arrivee")
    private LocalTime heureArrivee;

    @Column(name = "heure_sortie")
    private LocalTime heureSortie;

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
    public UUID getAppelId() { return appelId; }
    public void setAppelId(UUID appelId) { this.appelId = appelId; }
    public UUID getEleveId() { return eleveId; }
    public void setEleveId(UUID eleveId) { this.eleveId = eleveId; }
    public StatutPresence getStatut() { return statut; }
    public void setStatut(StatutPresence statut) { this.statut = statut; }
    public LocalTime getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(LocalTime heureArrivee) { this.heureArrivee = heureArrivee; }
    public LocalTime getHeureSortie() { return heureSortie; }
    public void setHeureSortie(LocalTime heureSortie) { this.heureSortie = heureSortie; }
}
