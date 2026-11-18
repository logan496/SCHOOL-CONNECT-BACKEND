package com.schoolconnect.api.presence.web;

import com.schoolconnect.api.presence.dto.AppelDtos.PresenceHistoriqueView;
import com.schoolconnect.api.presence.service.AppelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Historique des présences d'un élève : vue personnel d'établissement (CU-SUR-01) et vue parent (CU-PAR-05). */
@RestController
public class PresenceEleveController {

    private final AppelService service;

    public PresenceEleveController(AppelService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/etablissements/{etabId}/eleves/{eleveId}/presences")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public List<PresenceHistoriqueView> listPourEtablissement(
            @PathVariable UUID etabId, @PathVariable UUID eleveId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.listPourEleve(etabId, eleveId, from, to);
    }

    // Pas de restriction par rôle : tout utilisateur authentifié peut appeler cette route,
    // l'autorisation réelle (lien parent-enfant validé) est vérifiée dans AppelService.
    @GetMapping("/api/v1/mes-enfants/{eleveId}/presences")
    public List<PresenceHistoriqueView> listPourParent(
            @PathVariable UUID eleveId, Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.listPourParent(UUID.fromString(authentication.getName()), eleveId, from, to);
    }
}
