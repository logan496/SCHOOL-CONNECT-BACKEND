package com.schoolconnect.api.school.web;

import com.schoolconnect.api.school.dto.PeriodeDtos.CreatePeriodeRequest;
import com.schoolconnect.api.school.dto.PeriodeDtos.PeriodeView;
import com.schoolconnect.api.school.service.PeriodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Séquences/trimestres d'une année scolaire (CU-DIR-02), et ouverture/fermeture de la saisie des notes. */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}/annees-scolaires/{anneeId}/periodes")
public class PeriodeController {

    private static final String PEUT_GERER = "hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION:' + #etabId.toString())";

    private final PeriodeService service;

    public PeriodeController(PeriodeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PEUT_GERER)
    public PeriodeView create(@PathVariable UUID etabId, @PathVariable UUID anneeId,
                               @Valid @RequestBody CreatePeriodeRequest request) {
        return service.create(etabId, anneeId, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public List<PeriodeView> list(@PathVariable UUID etabId, @PathVariable UUID anneeId) {
        return service.list(etabId, anneeId);
    }

    @PatchMapping("/{periodeId}/ouvrir")
    @PreAuthorize(PEUT_GERER)
    public PeriodeView ouvrir(@PathVariable UUID etabId, @PathVariable UUID anneeId, @PathVariable UUID periodeId) {
        return service.setSaisieOuverte(etabId, anneeId, periodeId, true);
    }

    @PatchMapping("/{periodeId}/fermer")
    @PreAuthorize(PEUT_GERER)
    public PeriodeView fermer(@PathVariable UUID etabId, @PathVariable UUID anneeId, @PathVariable UUID periodeId) {
        return service.setSaisieOuverte(etabId, anneeId, periodeId, false);
    }
}
