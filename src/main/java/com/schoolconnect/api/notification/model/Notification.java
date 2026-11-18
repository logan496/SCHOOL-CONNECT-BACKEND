package com.schoolconnect.api.notification.model;

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

/** Notification in-app adressée à un utilisateur (parent/tuteur), éventuellement rattachée à un élève. */
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "etablissement_id")
    private UUID etablissementId;

    @Column(name = "eleve_id")
    private UUID eleveId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeNotification type;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(nullable = false)
    private String message;

    @Column(name = "lue_le")
    private Instant lueLe;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(UUID utilisateurId) { this.utilisateurId = utilisateurId; }
    public UUID getEtablissementId() { return etablissementId; }
    public void setEtablissementId(UUID etablissementId) { this.etablissementId = etablissementId; }
    public UUID getEleveId() { return eleveId; }
    public void setEleveId(UUID eleveId) { this.eleveId = eleveId; }
    public TypeNotification getType() { return type; }
    public void setType(TypeNotification type) { this.type = type; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getLueLe() { return lueLe; }
    public void setLueLe(Instant lueLe) { this.lueLe = lueLe; }
    public Instant getCreatedAt() { return createdAt; }
}
