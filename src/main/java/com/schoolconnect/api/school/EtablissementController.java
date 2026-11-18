package com.schoolconnect.api.school;

import com.schoolconnect.api.school.EtablissementDtos.CreateEtablissementRequest;
import com.schoolconnect.api.school.EtablissementDtos.EtablissementView;
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

@RestController
@RequestMapping("/api/v1/etablissements")
public class EtablissementController {

    private final EtablissementService service;

    public EtablissementController(EtablissementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public EtablissementView create(@Valid @RequestBody CreateEtablissementRequest request) {
        return service.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public List<EtablissementView> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #id.toString())")
    public EtablissementView get(@PathVariable("id") UUID id) {
        return service.get(id);
    }
}
