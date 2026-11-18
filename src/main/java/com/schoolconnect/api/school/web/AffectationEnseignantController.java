package com.schoolconnect.api.school.web;

import com.schoolconnect.api.school.dto.AffectationEnseignantDtos.AffectationView;
import com.schoolconnect.api.school.dto.AffectationEnseignantDtos.CreateAffectationRequest;
import com.schoolconnect.api.school.service.AffectationEnseignantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Affectation d'un enseignant à une classe/matière (CU-ENS-02). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}")
public class AffectationEnseignantController {

    private final AffectationEnseignantService service;

    public AffectationEnseignantController(AffectationEnseignantService service) {
        this.service = service;
    }

    @PostMapping("/affectations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION:' + #etabId.toString())")
    public AffectationView creer(@PathVariable UUID etabId, @Valid @RequestBody CreateAffectationRequest request) {
        return service.creer(etabId, request);
    }

    @GetMapping("/mes-affectations")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public List<AffectationView> mesAffectations(@PathVariable UUID etabId, Authentication authentication) {
        return service.listPourEnseignant(etabId, UUID.fromString(authentication.getName()));
    }
}
