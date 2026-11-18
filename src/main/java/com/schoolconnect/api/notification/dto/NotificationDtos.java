package com.schoolconnect.api.notification.dto;

import com.schoolconnect.api.notification.model.Notification;
import com.schoolconnect.api.notification.model.TypeNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class NotificationDtos {

    private NotificationDtos() {
    }

    public record NotificationView(
            UUID id, TypeNotification type, String titre, String message, UUID eleveId, UUID etablissementId,
            Instant creeLe, Instant lueLe) {

        public static NotificationView from(Notification n) {
            return new NotificationView(n.getId(), n.getType(), n.getTitre(), n.getMessage(), n.getEleveId(),
                    n.getEtablissementId(), n.getCreatedAt(), n.getLueLe());
        }
    }

    public record NotificationsPage(List<NotificationView> elements, int page, int taille, long total, long nonLues) {
    }

    public record CompteurNonLuesView(long nonLues) {
    }

    /** CU-SUR-07 : message libre envoyé par l'établissement aux parents/tuteurs d'un élève. */
    public record EnvoyerMessageRequest(
            @NotBlank @Size(max = 150) String titre,
            @NotBlank @Size(max = 2000) String message) {
    }

    public record EnvoiView(int destinataires) {
    }
}
