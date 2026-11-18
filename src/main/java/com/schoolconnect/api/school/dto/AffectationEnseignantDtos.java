package com.schoolconnect.api.school.dto;

import com.schoolconnect.api.school.model.AffectationEnseignant;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public final class AffectationEnseignantDtos {

    private AffectationEnseignantDtos() {
    }

    public record CreateAffectationRequest(
            @NotNull UUID classeId,
            @NotNull UUID matiereId,
            @NotNull UUID enseignantId) {
    }

    public record AffectationView(UUID id, UUID anneeScolaireId, UUID classeId, UUID matiereId, UUID enseignantId) {

        public static AffectationView from(AffectationEnseignant a) {
            return new AffectationView(a.getId(), a.getAnneeScolaireId(), a.getClasseId(), a.getMatiereId(), a.getEnseignantId());
        }
    }
}
