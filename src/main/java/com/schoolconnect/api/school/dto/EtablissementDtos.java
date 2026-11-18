package com.schoolconnect.api.school.dto;

import com.schoolconnect.api.school.model.Etablissement;
import com.schoolconnect.api.school.model.OrdreEnseignement;
import com.schoolconnect.api.school.model.SousSysteme;
import com.schoolconnect.api.school.model.StatutEtablissement;
import com.schoolconnect.api.school.model.TypeEtablissement;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class EtablissementDtos {

    private EtablissementDtos() {
    }

    public record CreateEtablissementRequest(
            @NotBlank @Size(max = 200) String nom,
            @NotBlank @Size(max = 30)
            @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Le code ne peut contenir que lettres, chiffres, tiret et underscore")
            String code,
            @NotNull TypeEtablissement type,
            @NotNull SousSysteme sousSysteme,
            @NotNull OrdreEnseignement ordreEnseignement,
            @Size(max = 100) String region,
            @Size(max = 100) String departement,
            @Size(max = 100) String ville,
            @Size(max = 255) String adresse,
            @Size(max = 20) String telephone,
            @Email @Size(max = 150) String email) {
    }

    public record EtablissementView(
            UUID id,
            String nom,
            String code,
            TypeEtablissement type,
            SousSysteme sousSysteme,
            OrdreEnseignement ordreEnseignement,
            String region,
            String departement,
            String ville,
            String adresse,
            String telephone,
            String email,
            String devise,
            StatutEtablissement statut) {

        public static EtablissementView from(Etablissement e) {
            return new EtablissementView(e.getId(), e.getNom(), e.getCode(), e.getType(), e.getSousSysteme(),
                    e.getOrdreEnseignement(), e.getRegion(), e.getDepartement(), e.getVille(), e.getAdresse(),
                    e.getTelephone(), e.getEmail(), e.getDevise(), e.getStatut());
        }
    }
}
