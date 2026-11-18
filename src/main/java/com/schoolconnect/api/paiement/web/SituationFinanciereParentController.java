package com.schoolconnect.api.paiement.web;

import com.schoolconnect.api.paiement.dto.PaiementDtos.SituationFinanciereView;
import com.schoolconnect.api.paiement.service.PaiementService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** CU-PAR-10/12 : situation financière et historique des reçus, côté parent. */
@RestController
public class SituationFinanciereParentController {

    private final PaiementService service;

    public SituationFinanciereParentController(PaiementService service) {
        this.service = service;
    }

    // Pas de restriction par rôle : l'autorisation réelle (lien parent-enfant validé) est vérifiée dans PaiementService.
    @GetMapping("/api/v1/mes-enfants/{eleveId}/situation-financiere")
    public SituationFinanciereView situation(@PathVariable UUID eleveId, Authentication authentication) {
        return service.situationFinancierePourParent(UUID.fromString(authentication.getName()), eleveId);
    }
}
