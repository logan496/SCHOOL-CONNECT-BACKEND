package com.schoolconnect.api.note.web;

import com.schoolconnect.api.note.dto.NoteDtos.CorrigerNotesRequest;
import com.schoolconnect.api.note.dto.NoteDtos.CreerEvaluationRequest;
import com.schoolconnect.api.note.dto.NoteDtos.EvaluationView;
import com.schoolconnect.api.note.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Évaluations et saisie des notes (CU-ENS-04, CU-ENS-05). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}/classes/{classeId}/matieres/{matiereId}/evaluations")
@PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ENSEIGNANT:' + #etabId.toString())"
        + " or hasAuthority('DIRECTION:' + #etabId.toString())")
public class EvaluationController {

    private final NoteService service;

    public EvaluationController(NoteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationView creer(@PathVariable UUID etabId, @PathVariable UUID classeId, @PathVariable UUID matiereId,
                                 @Valid @RequestBody CreerEvaluationRequest request, Authentication authentication) {
        return service.creerEvaluation(etabId, classeId, matiereId, request,
                UUID.fromString(authentication.getName()), estDirection(authentication, etabId));
    }

    @PatchMapping("/{evaluationId}")
    public EvaluationView corriger(@PathVariable UUID etabId, @PathVariable UUID classeId, @PathVariable UUID matiereId,
                                    @PathVariable UUID evaluationId, @Valid @RequestBody CorrigerNotesRequest request,
                                    Authentication authentication) {
        return service.corrigerNotes(etabId, classeId, matiereId, evaluationId, request,
                UUID.fromString(authentication.getName()), estDirection(authentication, etabId));
    }

    private static boolean estDirection(Authentication authentication, UUID etabId) {
        return authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("SUPER_ADMIN") || a.getAuthority().equals("DIRECTION:" + etabId));
    }
}
