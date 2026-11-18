package com.schoolconnect.api.presence.dto;

import com.schoolconnect.api.eleve.dto.EleveDtos.EleveView;
import com.schoolconnect.api.presence.model.StatutPresence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class AppelDtos {

    private AppelDtos() {
    }

    public record PresenceSaisie(
            @NotNull UUID eleveId,
            @NotNull StatutPresence statut,
            LocalTime heureArrivee,
            LocalTime heureSortie) {
    }

    /** Seules les exceptions sont à fournir : le reste de la classe est marqué PRESENT par défaut. */
    public record SaisirAppelRequest(
            @NotNull LocalDate date,
            UUID matiereId,
            @NotNull List<@Valid PresenceSaisie> presences) {
    }

    public record CorrigerAppelRequest(@NotEmpty List<@Valid PresenceSaisie> presences) {
    }

    public record PresenceEleveView(
            UUID id, UUID eleveId, StatutPresence statut, LocalTime heureArrivee, LocalTime heureSortie,
            EleveView eleve) {
    }

    public record AppelView(
            UUID id, UUID classeId, UUID anneeScolaireId, LocalDate date, UUID matiereId,
            List<PresenceEleveView> presences) {
    }

    public record PresenceHistoriqueView(
            LocalDate date, StatutPresence statut, LocalTime heureArrivee, LocalTime heureSortie, UUID classeId) {
    }
}
