package com.schoolconnect.api.eleve.web;

import com.schoolconnect.api.eleve.dto.EleveDtos.AffecterClasseRequest;
import com.schoolconnect.api.eleve.dto.EleveDtos.EnregistrerTuteurRequest;
import com.schoolconnect.api.eleve.dto.EleveDtos.InscriptionView;
import com.schoolconnect.api.eleve.dto.EleveDtos.InscrireEleveRequest;
import com.schoolconnect.api.eleve.dto.EleveDtos.ParentEleveView;
import com.schoolconnect.api.eleve.dto.EleveDtos.TuteurLiaisonResult;
import com.schoolconnect.api.eleve.service.EleveService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Flux secrétariat : inscription élève, affectation en classe, tuteurs (CU-SEC-01, 03, 04, 05). */
@RestController
@RequestMapping("/api/v1/etablissements/{etabId}/eleves")
@PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('SECRETARIAT:' + #etabId.toString()) or hasAuthority('DIRECTION:' + #etabId.toString())")
public class EleveController {

    private final EleveService service;

    public EleveController(EleveService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InscriptionView inscrire(@PathVariable UUID etabId, @Valid @RequestBody InscrireEleveRequest request) {
        return service.inscrire(etabId, request);
    }

    @GetMapping
    public List<InscriptionView> list(@PathVariable UUID etabId, @RequestParam UUID anneeScolaireId) {
        return service.list(etabId, anneeScolaireId);
    }

    @PatchMapping("/{eleveId}/classe")
    public InscriptionView affecterClasse(@PathVariable UUID etabId, @PathVariable UUID eleveId,
                                           @Valid @RequestBody AffecterClasseRequest request) {
        return service.affecterClasse(etabId, eleveId, request);
    }

    @PostMapping("/{eleveId}/tuteurs")
    @ResponseStatus(HttpStatus.CREATED)
    public TuteurLiaisonResult enregistrerTuteur(@PathVariable UUID etabId, @PathVariable UUID eleveId,
                                                  @Valid @RequestBody EnregistrerTuteurRequest request,
                                                  Authentication authentication) {
        return service.enregistrerTuteur(etabId, eleveId, UUID.fromString(authentication.getName()), request);
    }

    @GetMapping("/{eleveId}/tuteurs")
    public List<ParentEleveView> listTuteurs(@PathVariable UUID etabId, @PathVariable UUID eleveId) {
        return service.listTuteurs(etabId, eleveId);
    }

    @PatchMapping("/{eleveId}/tuteurs/{parentEleveId}/valider")
    public ParentEleveView validerLiaison(@PathVariable UUID etabId, @PathVariable UUID eleveId,
                                           @PathVariable UUID parentEleveId, Authentication authentication) {
        return service.validerLiaison(etabId, eleveId, parentEleveId, UUID.fromString(authentication.getName()));
    }

    @PatchMapping("/{eleveId}/tuteurs/{parentEleveId}/rejeter")
    public ParentEleveView rejeterLiaison(@PathVariable UUID etabId, @PathVariable UUID eleveId,
                                           @PathVariable UUID parentEleveId, Authentication authentication) {
        return service.rejeterLiaison(etabId, eleveId, parentEleveId, UUID.fromString(authentication.getName()));
    }
}
