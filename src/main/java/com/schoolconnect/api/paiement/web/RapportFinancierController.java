package com.schoolconnect.api.paiement.web;

import com.schoolconnect.api.paiement.dto.PaiementDtos.ImpayeView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.RapportCaisseView;
import com.schoolconnect.api.paiement.service.PaiementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Suivi des impayés (CU-ECO-06, CU-DIR-07) et rapport de caisse journalier (CU-ECO-08). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}")
@PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ECONOMAT:' + #etabId.toString())"
        + " or hasAuthority('DIRECTION:' + #etabId.toString())")
public class RapportFinancierController {

    private final PaiementService service;

    public RapportFinancierController(PaiementService service) {
        this.service = service;
    }

    @GetMapping("/impayes")
    public List<ImpayeView> impayes(@PathVariable UUID etabId) {
        return service.listImpayes(etabId);
    }

    @GetMapping("/rapport-caisse")
    public RapportCaisseView rapportCaisse(@PathVariable UUID etabId,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.rapportCaisse(etabId, date);
    }
}
