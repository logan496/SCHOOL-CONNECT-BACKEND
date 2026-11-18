package com.schoolconnect.api.school.model;

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

@Entity
@Table(name = "periode")
public class Periode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "annee_scolaire_id", nullable = false)
    private UUID anneeScolaireId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypePeriode type;

    @Column(nullable = false)
    private int numero;

    @Column(name = "periode_parent_id")
    private UUID periodeParentId;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "saisie_ouverte", nullable = false)
    private boolean saisieOuverte = false;

    @Column(nullable = false)
    private boolean publiee = false;

    public UUID getId() { return id; }
    public UUID getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(UUID anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }
    public TypePeriode getType() { return type; }
    public void setType(TypePeriode type) { this.type = type; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public UUID getPeriodeParentId() { return periodeParentId; }
    public void setPeriodeParentId(UUID periodeParentId) { this.periodeParentId = periodeParentId; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public boolean isSaisieOuverte() { return saisieOuverte; }
    public void setSaisieOuverte(boolean saisieOuverte) { this.saisieOuverte = saisieOuverte; }
    public boolean isPubliee() { return publiee; }
    public void setPubliee(boolean publiee) { this.publiee = publiee; }
}
