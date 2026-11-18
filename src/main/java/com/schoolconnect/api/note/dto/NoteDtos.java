package com.schoolconnect.api.note.dto;

import com.schoolconnect.api.eleve.dto.EleveDtos.EleveView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class NoteDtos {

    private NoteDtos() {
    }

    public record SaisieNote(@NotNull UUID eleveId, BigDecimal valeur, boolean absent) {
    }

    /** Les élèves omis restent sans note (non noté) ; ils pourront être complétés via la correction. */
    public record CreerEvaluationRequest(
            @NotNull UUID periodeId,
            @NotEmpty @Size(max = 100) String nom,
            @NotNull @DecimalMin("0.1") BigDecimal coefficient,
            @NotNull @DecimalMin("1") BigDecimal bareme,
            @NotNull LocalDate date,
            @NotNull List<@Valid SaisieNote> notes) {
    }

    public record CorrigerNotesRequest(@NotEmpty List<@Valid SaisieNote> notes) {
    }

    public record NoteView(UUID id, UUID eleveId, BigDecimal valeur, boolean absent, EleveView eleve) {
    }

    public record EvaluationView(
            UUID id, UUID classeId, UUID matiereId, UUID periodeId, String nom, BigDecimal coefficient,
            BigDecimal bareme, LocalDate date, List<NoteView> notes) {
    }

    public record NoteHistoriqueView(
            UUID evaluationId, String evaluationNom, LocalDate date, BigDecimal coefficient, BigDecimal bareme,
            BigDecimal valeur, boolean absent) {
    }

    public record MatiereNotesView(UUID matiereId, BigDecimal moyenne, List<NoteHistoriqueView> notes) {
    }
}
