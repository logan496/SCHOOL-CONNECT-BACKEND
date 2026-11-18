package com.schoolconnect.api.school.dto;

import com.schoolconnect.api.school.model.Matiere;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class MatiereDtos {

    private MatiereDtos() {
    }

    public record CreateMatiereRequest(
            @NotBlank @Size(max = 100) String nom,
            @NotBlank @Size(max = 20) String code,
            @NotNull @DecimalMin("0.1") BigDecimal coefficientDefaut) {
    }

    public record MatiereView(UUID id, UUID etablissementId, String nom, String code, BigDecimal coefficientDefaut) {

        public static MatiereView from(Matiere m) {
            return new MatiereView(m.getId(), m.getEtablissementId(), m.getNom(), m.getCode(), m.getCoefficientDefaut());
        }
    }
}
