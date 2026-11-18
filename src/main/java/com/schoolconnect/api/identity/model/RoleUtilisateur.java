package com.schoolconnect.api.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Rôle d'un utilisateur dans un établissement (etablissementId est null pour SUPER_ADMIN). */
@Entity
@Table(name = "role_utilisateur")
public class RoleUtilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "etablissement_id")
    private UUID etablissementId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType role;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    protected RoleUtilisateur() {
    }

    public RoleUtilisateur(UUID utilisateurId, UUID etablissementId, RoleType role) {
        this.utilisateurId = utilisateurId;
        this.etablissementId = etablissementId;
        this.role = role;
    }

    public UUID getId() { return id; }
    public UUID getUtilisateurId() { return utilisateurId; }
    public UUID getEtablissementId() { return etablissementId; }
    public RoleType getRole() { return role; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
}
