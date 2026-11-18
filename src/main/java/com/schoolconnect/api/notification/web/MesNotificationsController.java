package com.schoolconnect.api.notification.web;

import com.schoolconnect.api.notification.dto.NotificationDtos.CompteurNonLuesView;
import com.schoolconnect.api.notification.dto.NotificationDtos.NotificationView;
import com.schoolconnect.api.notification.dto.NotificationDtos.NotificationsPage;
import com.schoolconnect.api.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** CU-PAR-13 : notifications de l'utilisateur connecté. Chacun ne voit que les siennes. */
@RestController
@RequestMapping("/api/v1/mes-notifications")
public class MesNotificationsController {

    private final NotificationService service;

    public MesNotificationsController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public NotificationsPage lister(Authentication authentication,
                                    @RequestParam(defaultValue = "false") boolean nonLues,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int taille) {
        return service.mesNotifications(utilisateur(authentication), nonLues, page, taille);
    }

    @GetMapping("/non-lues/nombre")
    public CompteurNonLuesView nombreNonLues(Authentication authentication) {
        return service.compterNonLues(utilisateur(authentication));
    }

    @PatchMapping("/{notificationId}/lue")
    public NotificationView marquerLue(@PathVariable UUID notificationId, Authentication authentication) {
        return service.marquerLue(utilisateur(authentication), notificationId);
    }

    @PatchMapping("/lues")
    public CompteurNonLuesView marquerToutesLues(Authentication authentication) {
        return service.marquerToutesLues(utilisateur(authentication));
    }

    private static UUID utilisateur(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
