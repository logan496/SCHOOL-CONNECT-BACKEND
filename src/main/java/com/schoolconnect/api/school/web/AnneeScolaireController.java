package com.schoolconnect.api.school.web;

import com.schoolconnect.api.school.dto.AnneeScolaireDtos.AnneeScolaireView;
import com.schoolconnect.api.school.dto.AnneeScolaireDtos.CreateAnneeScolaireRequest;
import com.schoolconnect.api.school.service.AnneeScolaireService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Paramétrage de l'année scolaire (CU-DIR-02) — nécessaire aux classes et inscriptions. */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}/annees-scolaires")
public class AnneeScolaireController {

    private final AnneeScolaireService service;

    public AnneeScolaireController(AnneeScolaireService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION:' + #etabId.toString())")
    public AnneeScolaireView create(@PathVariable UUID etabId, @Valid @RequestBody CreateAnneeScolaireRequest request) {
        return service.create(etabId, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public List<AnneeScolaireView> list(@PathVariable UUID etabId) {
        return service.list(etabId);
    }
}
