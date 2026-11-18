package com.schoolconnect.api.note.web;

import com.schoolconnect.api.note.dto.NoteDtos.MatiereNotesView;
import com.schoolconnect.api.note.service.NoteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Notes d'un élève, par séquence : vue personnel d'établissement et vue parent (CU-PAR-07). */
@RestController
public class NoteEleveController {

    private final NoteService service;

    public NoteEleveController(NoteService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/etablissements/{etabId}/eleves/{eleveId}/notes")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ETAB:' + #etabId.toString())")
    public List<MatiereNotesView> listPourEtablissement(
            @PathVariable UUID etabId, @PathVariable UUID eleveId,
            @RequestParam UUID periodeId, @RequestParam(required = false) UUID matiereId) {
        return service.listPourEleve(etabId, eleveId, periodeId, matiereId);
    }

    // Pas de restriction par rôle : l'autorisation réelle (lien parent-enfant validé) est vérifiée dans NoteService.
    @GetMapping("/api/v1/mes-enfants/{eleveId}/notes")
    public List<MatiereNotesView> listPourParent(
            @PathVariable UUID eleveId, Authentication authentication,
            @RequestParam UUID periodeId, @RequestParam(required = false) UUID matiereId) {
        return service.listPourParent(UUID.fromString(authentication.getName()), eleveId, periodeId, matiereId);
    }
}
