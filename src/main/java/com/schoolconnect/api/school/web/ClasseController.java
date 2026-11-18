package com.schoolconnect.api.school.web;

import com.schoolconnect.api.school.dto.ClasseDtos.ClasseView;
import com.schoolconnect.api.school.dto.ClasseDtos.CreateClasseRequest;
import com.schoolconnect.api.school.service.ClasseService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Paramétrage des classes (CU-DIR-02, CU-SEC-03). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}/classes")
public class ClasseController {

    private final ClasseService service;

    public ClasseController(ClasseService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION:' + #etabId.toString()) or hasAuthority('SECRETARIAT:' + #etabId.toString())")
    public ClasseView create(@PathVariable UUID etabId, @Valid @RequestBody CreateClasseRequest request) {
        return service.create(etabId, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public List<ClasseView> list(@PathVariable UUID etabId, @RequestParam UUID anneeScolaireId) {
        return service.list(etabId, anneeScolaireId);
    }
}
