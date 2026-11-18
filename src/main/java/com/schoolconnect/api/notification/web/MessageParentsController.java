package com.schoolconnect.api.notification.web;

import com.schoolconnect.api.notification.dto.NotificationDtos.EnvoiView;
import com.schoolconnect.api.notification.dto.NotificationDtos.EnvoyerMessageRequest;
import com.schoolconnect.api.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** CU-SUR-07 : l'établissement déclenche une notification aux parents/tuteurs d'un élève. */
@RestController
public class MessageParentsController {

    private final NotificationService service;

    public MessageParentsController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/etablissements/{etabId}/eleves/{eleveId}/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION:' + #etabId.toString())"
            + " or hasAuthority('SURVEILLANT:' + #etabId.toString()) or hasAuthority('SECRETARIAT:' + #etabId.toString())")
    public EnvoiView envoyer(@PathVariable UUID etabId, @PathVariable UUID eleveId,
                             @Valid @RequestBody EnvoyerMessageRequest request) {
        return service.envoyerMessage(etabId, eleveId, request);
    }
}
