package com.schoolconnect.api.paiement.web;

import com.schoolconnect.api.paiement.dto.PaiementDtos.ImpayeView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.RapportCaisseView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.RelanceView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.RelancerImpayesRequest;
import com.schoolconnect.api.paiement.service.PaiementService;
import com.schoolconnect.api.paiement.service.RelanceImpayesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Suivi et relance des impayés (CU-ECO-06, CU-DIR-07) et rapport de caisse journalier (CU-ECO-08). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}")
@PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ECONOMAT:' + #etabId.toString())"
        + " or hasAuthority('DIRECTION:' + #etabId.toString())")
public class RapportFinancierController {

    private final PaiementService service;
    private final RelanceImpayesService relances;

    public RapportFinancierController(PaiementService service, RelanceImpayesService relances) {
        this.service = service;
        this.relances = relances;
    }

    @GetMapping("/impayes")
    public List<ImpayeView> impayes(@PathVariable UUID etabId) {
        return service.listImpayes(etabId);
    }

    /** Relance manuelle des parents pour les montants échus non réglés (sans délai anti-doublon). */
    @PostMapping("/impayes/relances")
    public RelanceView relancer(@PathVariable UUID etabId, @RequestBody(required = false) RelancerImpayesRequest request) {
        return relances.relancer(etabId, request == null ? null : request.eleveIds(), null);
    }

    @GetMapping("/rapport-caisse")
    public RapportCaisseView rapportCaisse(@PathVariable UUID etabId,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.rapportCaisse(etabId, date);
    }
}
