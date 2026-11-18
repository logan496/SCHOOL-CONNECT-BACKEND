package com.schoolconnect.api.school.web;

import com.schoolconnect.api.school.dto.MatiereDtos.CreateMatiereRequest;
import com.schoolconnect.api.school.dto.MatiereDtos.MatiereView;
import com.schoolconnect.api.school.service.MatiereService;
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

/** Paramétrage des matières (CU-DIR-02). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}/matieres")
public class MatiereController {

    private final MatiereService service;

    public MatiereController(MatiereService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION:' + #etabId.toString())")
    public MatiereView create(@PathVariable UUID etabId, @Valid @RequestBody CreateMatiereRequest request) {
        return service.create(etabId, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public List<MatiereView> list(@PathVariable UUID etabId) {
        return service.list(etabId);
    }
}
