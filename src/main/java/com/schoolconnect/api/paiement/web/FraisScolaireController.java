package com.schoolconnect.api.paiement.web;

import com.schoolconnect.api.paiement.dto.PaiementDtos.CreerFraisScolaireRequest;
import com.schoolconnect.api.paiement.dto.PaiementDtos.FraisScolaireView;
import com.schoolconnect.api.paiement.service.PaiementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Grille tarifaire (CU-DIR-04). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}/annees-scolaires/{anneeId}/frais-scolaires")
public class FraisScolaireController {

    private final PaiementService service;

    public FraisScolaireController(PaiementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION:' + #etabId.toString())")
    public FraisScolaireView creer(@PathVariable UUID etabId, @PathVariable UUID anneeId,
                                    @Valid @RequestBody CreerFraisScolaireRequest request) {
        return service.creerFraisScolaire(etabId, anneeId, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public List<FraisScolaireView> list(@PathVariable UUID etabId, @PathVariable UUID anneeId) {
        return service.listFraisScolaire(etabId, anneeId);
    }
}
