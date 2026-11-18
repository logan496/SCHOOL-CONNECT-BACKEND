package com.schoolconnect.api.school.dto;

import com.schoolconnect.api.school.model.Periode;
import com.schoolconnect.api.school.model.TypePeriode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public final class PeriodeDtos {

    private PeriodeDtos() {
    }

    public record CreatePeriodeRequest(
            @NotNull TypePeriode type,
            @Min(1) int numero,
            UUID periodeParentId,
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin) {
    }

    public record PeriodeView(
            UUID id, UUID anneeScolaireId, TypePeriode type, int numero, UUID periodeParentId,
            LocalDate dateDebut, LocalDate dateFin, boolean saisieOuverte, boolean publiee) {

        public static PeriodeView from(Periode p) {
            return new PeriodeView(p.getId(), p.getAnneeScolaireId(), p.getType(), p.getNumero(),
                    p.getPeriodeParentId(), p.getDateDebut(), p.getDateFin(), p.isSaisieOuverte(), p.isPubliee());
        }
    }
}
