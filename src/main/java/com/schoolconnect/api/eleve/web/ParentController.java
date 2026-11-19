package com.schoolconnect.api.eleve.web;

import com.schoolconnect.api.eleve.dto.EleveDtos.EnfantView;
import com.schoolconnect.api.eleve.dto.EleveDtos.LierParCodeRequest;
import com.schoolconnect.api.eleve.dto.EleveDtos.ParentEleveView;
import com.schoolconnect.api.eleve.service.ParentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Flux parent : rattachement à un élève via code de liaison, consultation de ses enfants.
 * Pas de restriction par rôle car le profil "parent" découle des liens parent_eleve,
 * pas d'un RoleType. L'accès est vérifié dans le service via le lien parent-élève.
 */
@RestController
@RequestMapping("/api/v1/mes-enfants")
public class ParentController {

    private final ParentService service;

    public ParentController(ParentService service) {
        this.service = service;
    }

    /** CU-PAR-01 : le parent saisit un code de liaison pour se rattacher à un élève. */
    @PostMapping("/lier")
    @ResponseStatus(HttpStatus.CREATED)
    public ParentEleveView lierParCode(@Valid @RequestBody LierParCodeRequest request,
                                        Authentication authentication) {
        return service.lierParCode(UUID.fromString(authentication.getName()), request);
    }

    /** CU-PAR-02 : liste des enfants du parent connecté (tous statuts). */
    @GetMapping
    public List<EnfantView> listerEnfants(Authentication authentication) {
        return service.listerEnfants(UUID.fromString(authentication.getName()));
    }

    /** CU-PAR-03 : détail d'un enfant rattaché (statut VALIDE uniquement). */
    @GetMapping("/{eleveId}")
    public EnfantView consulterEnfant(@PathVariable UUID eleveId, Authentication authentication) {
        return service.consulterEnfant(UUID.fromString(authentication.getName()), eleveId);
    }
}
