package com.schoolconnect.api.presence.web;

import com.schoolconnect.api.presence.dto.AppelDtos.AppelView;
import com.schoolconnect.api.presence.dto.AppelDtos.CorrigerAppelRequest;
import com.schoolconnect.api.presence.dto.AppelDtos.SaisirAppelRequest;
import com.schoolconnect.api.presence.service.AppelService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/** Appel de classe (CU-ENS-03, CU-SUR-01/02/04). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}/classes/{classeId}/appels")
public class AppelController {

    private static final String PEUT_SAISIR =
            "hasAuthority('SUPER_ADMIN') or hasAuthority('ENSEIGNANT:' + #etabId.toString())"
                    + " or hasAuthority('SURVEILLANT:' + #etabId.toString()) or hasAuthority('DIRECTION:' + #etabId.toString())";

    private final AppelService service;

    public AppelController(AppelService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PEUT_SAISIR)
    public AppelView saisir(@PathVariable UUID etabId, @PathVariable UUID classeId,
                             @Valid @RequestBody SaisirAppelRequest request, Authentication authentication) {
        return service.saisirAppel(etabId, classeId, request, UUID.fromString(authentication.getName()));
    }

    @PatchMapping("/{appelId}")
    @PreAuthorize(PEUT_SAISIR)
    public AppelView corriger(@PathVariable UUID etabId, @PathVariable UUID classeId, @PathVariable UUID appelId,
                               @Valid @RequestBody CorrigerAppelRequest request) {
        return service.corrigerAppel(etabId, classeId, appelId, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public AppelView getParDate(@PathVariable UUID etabId, @PathVariable UUID classeId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getParClasseEtDate(etabId, classeId, date);
    }
}
