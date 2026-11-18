package com.schoolconnect.api.school.dto;

import com.schoolconnect.api.school.model.AnneeScolaire;
import com.schoolconnect.api.school.model.StatutAnnee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public final class AnneeScolaireDtos {

    private AnneeScolaireDtos() {
    }

    public record CreateAnneeScolaireRequest(
            @NotBlank @Size(max = 20) String libelle,
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin,
            boolean courante) {
    }

    public record AnneeScolaireView(
            UUID id, UUID etablissementId, String libelle, LocalDate dateDebut, LocalDate dateFin,
            boolean courante, StatutAnnee statut) {

        public static AnneeScolaireView from(AnneeScolaire a) {
            return new AnneeScolaireView(a.getId(), a.getEtablissementId(), a.getLibelle(), a.getDateDebut(),
                    a.getDateFin(), a.isCourante(), a.getStatut());
        }
    }
}
