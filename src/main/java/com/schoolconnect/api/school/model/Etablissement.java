package com.schoolconnect.api.school.model;

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
import java.util.UUID;

@Entity
@Table(name = "etablissement")
public class Etablissement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(nullable = false, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeEtablissement type;

    @Enumerated(EnumType.STRING)
    @Column(name = "sous_systeme", nullable = false, length = 20)
    private SousSysteme sousSysteme;

    @Enumerated(EnumType.STRING)
    @Column(name = "ordre_enseignement", nullable = false, length = 20)
    private OrdreEnseignement ordreEnseignement;

    @Column(length = 100)
    private String region;

    @Column(length = 100)
    private String departement;

    @Column(length = 100)
    private String ville;

    @Column(length = 255)
    private String adresse;

    @Column(length = 20)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false, length = 3)
    private String devise = "XAF";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutEtablissement statut = StatutEtablissement.ACTIF;

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
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public TypeEtablissement getType() { return type; }
    public void setType(TypeEtablissement type) { this.type = type; }
    public SousSysteme getSousSysteme() { return sousSysteme; }
    public void setSousSysteme(SousSysteme sousSysteme) { this.sousSysteme = sousSysteme; }
    public OrdreEnseignement getOrdreEnseignement() { return ordreEnseignement; }
    public void setOrdreEnseignement(OrdreEnseignement ordreEnseignement) { this.ordreEnseignement = ordreEnseignement; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }
    public StatutEtablissement getStatut() { return statut; }
    public void setStatut(StatutEtablissement statut) { this.statut = statut; }
}
