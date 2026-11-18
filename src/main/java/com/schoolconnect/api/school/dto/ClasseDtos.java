package com.schoolconnect.api.school.dto;

import com.schoolconnect.api.school.model.Classe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class ClasseDtos {

    private ClasseDtos() {
    }

    public record CreateClasseRequest(
            @NotNull UUID anneeScolaireId,
            @NotBlank @Size(max = 50) String nom,
            @NotBlank @Size(max = 30) String niveau,
            @Positive Integer effectifMax) {
    }

    public record ClasseView(UUID id, UUID etablissementId, UUID anneeScolaireId, String nom, String niveau, Integer effectifMax) {

        public static ClasseView from(Classe c) {
            return new ClasseView(c.getId(), c.getEtablissementId(), c.getAnneeScolaireId(), c.getNom(), c.getNiveau(), c.getEffectifMax());
        }
    }
}
