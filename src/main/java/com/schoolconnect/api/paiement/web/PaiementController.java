package com.schoolconnect.api.paiement.web;

import com.schoolconnect.api.paiement.dto.PaiementDtos.AnnulerPaiementRequest;
import com.schoolconnect.api.paiement.dto.PaiementDtos.EnregistrerPaiementRequest;
import com.schoolconnect.api.paiement.dto.PaiementDtos.PaiementView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.SituationFinanciereView;
import com.schoolconnect.api.paiement.service.PaiementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Enregistrement et annulation des paiements d'un élève (CU-ECO-02/03/07), situation financière (CU-ECO-01). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}/eleves/{eleveId}")
public class PaiementController {

    private static final String PEUT_GERER_CAISSE =
            "hasAuthority('SUPER_ADMIN') or hasAuthority('ECONOMAT:' + #etabId.toString()) or hasAuthority('DIRECTION:' + #etabId.toString())";

    private final PaiementService service;

    public PaiementController(PaiementService service) {
        this.service = service;
    }

    @PostMapping("/paiements")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PEUT_GERER_CAISSE)
    public PaiementView enregistrer(@PathVariable UUID etabId, @PathVariable UUID eleveId,
                                     @Valid @RequestBody EnregistrerPaiementRequest request, Authentication authentication) {
        return service.enregistrerPaiement(etabId, eleveId, request, UUID.fromString(authentication.getName()));
    }

    @PatchMapping("/paiements/{paiementId}/annuler")
    @PreAuthorize(PEUT_GERER_CAISSE)
    public PaiementView annuler(@PathVariable UUID etabId, @PathVariable UUID eleveId, @PathVariable UUID paiementId,
                                 @Valid @RequestBody AnnulerPaiementRequest request, Authentication authentication) {
        return service.annulerPaiement(etabId, eleveId, paiementId, request, UUID.fromString(authentication.getName()));
    }

    @GetMapping("/situation-financiere")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public SituationFinanciereView situation(@PathVariable UUID etabId, @PathVariable UUID eleveId) {
        return service.situationFinanciere(etabId, eleveId);
    }
}
