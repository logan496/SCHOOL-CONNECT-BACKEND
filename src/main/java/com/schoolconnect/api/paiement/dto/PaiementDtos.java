package com.schoolconnect.api.paiement.dto;

import com.schoolconnect.api.eleve.dto.EleveDtos.EleveView;
import com.schoolconnect.api.paiement.model.ModePaiement;
import com.schoolconnect.api.paiement.model.StatutPaiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PaiementDtos {

    private PaiementDtos() {
    }

    public record CreerFraisScolaireRequest(
            @NotBlank @Size(max = 30) String niveau,
            @NotBlank @Size(max = 100) String libelle,
            @NotNull @DecimalMin("0") BigDecimal montant,
            @NotNull LocalDate dateEcheance) {
    }

    public record FraisScolaireView(
            UUID id, UUID anneeScolaireId, String niveau, String libelle, BigDecimal montant, LocalDate dateEcheance) {
    }

    public record EnregistrerPaiementRequest(
            @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
            @NotNull ModePaiement mode,
            @Size(max = 100) String reference,
            @NotNull LocalDate date) {
    }

    public record AnnulerPaiementRequest(@NotBlank @Size(max = 500) String motif) {
    }

    public record PaiementView(
            UUID id, UUID eleveId, BigDecimal montant, ModePaiement mode, String reference, LocalDate date,
            String numeroRecu, StatutPaiement statut) {
    }

    public record SituationFinanciereView(
            UUID eleveId, EleveView eleve, BigDecimal totalDu, BigDecimal totalPaye, BigDecimal solde,
            List<FraisScolaireView> fraisApplicables, List<PaiementView> paiements) {
    }

    public record ImpayeView(UUID eleveId, EleveView eleve, BigDecimal totalDu, BigDecimal totalPaye, BigDecimal solde) {
    }

    public record RapportCaisseLigne(ModePaiement mode, BigDecimal total, long nombre) {
    }

    public record RapportCaisseView(LocalDate date, BigDecimal total, List<RapportCaisseLigne> parMode) {
    }

    /** Relance manuelle : sans liste, tous les élèves en impayé échu sont relancés. */
    public record RelancerImpayesRequest(Set<UUID> eleveIds) {
    }

    public record RelanceView(int elevesEnImpaye, int elevesRelances, int notificationsEnvoyees, int elevesSansParentLie) {
    }
}
