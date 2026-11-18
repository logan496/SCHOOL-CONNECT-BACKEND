package com.schoolconnect.api.notification.service;

import com.schoolconnect.api.eleve.model.ParentEleve;
import com.schoolconnect.api.eleve.model.StatutParentEleve;
import com.schoolconnect.api.eleve.repository.InscriptionRepository;
import com.schoolconnect.api.eleve.repository.ParentEleveRepository;
import com.schoolconnect.api.identity.model.Utilisateur;
import com.schoolconnect.api.identity.repository.UtilisateurRepository;
import com.schoolconnect.api.notification.dto.NotificationDtos.CompteurNonLuesView;
import com.schoolconnect.api.notification.dto.NotificationDtos.EnvoiView;
import com.schoolconnect.api.notification.dto.NotificationDtos.EnvoyerMessageRequest;
import com.schoolconnect.api.notification.dto.NotificationDtos.NotificationView;
import com.schoolconnect.api.notification.dto.NotificationDtos.NotificationsPage;
import com.schoolconnect.api.notification.model.Notification;
import com.schoolconnect.api.notification.model.TypeNotification;
import com.schoolconnect.api.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Création et consultation des notifications (CU-SYS-01/03, CU-PAR-13, CU-SUR-07).
 * Les notifications sont enregistrées dans la transaction de l'action qui les déclenche
 * (appel, paiement…) : si l'action échoue, aucune notification n'est produite. Les canaux
 * externes éventuels sont sollicités après validation de cette transaction.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int TAILLE_MAX = 100;

    private final NotificationRepository notifications;
    private final ParentEleveRepository parentsEleves;
    private final InscriptionRepository inscriptions;
    private final UtilisateurRepository utilisateurs;
    private final List<CanalNotificationExterne> canauxExternes;
    private final ApplicationEventPublisher events;

    public NotificationService(NotificationRepository notifications, ParentEleveRepository parentsEleves,
                               InscriptionRepository inscriptions, UtilisateurRepository utilisateurs,
                               List<CanalNotificationExterne> canauxExternes, ApplicationEventPublisher events) {
        this.notifications = notifications;
        this.parentsEleves = parentsEleves;
        this.inscriptions = inscriptions;
        this.utilisateurs = utilisateurs;
        this.canauxExternes = canauxExternes;
        this.events = events;
    }

    /** Publié après création de notifications, pour l'envoi sur les canaux externes une fois la transaction validée. */
    public record NotificationsCreees(List<UUID> ids) {
    }

    /**
     * Notifie chaque parent/tuteur ayant un lien validé et le droit de consulter l'élève.
     * Retourne le nombre de notifications créées (0 si l'élève n'a aucun parent lié).
     */
    @Transactional
    public int notifierParents(UUID etablissementId, UUID eleveId, TypeNotification type, String titre, String message) {
        List<UUID> destinataires = parentsEleves.findByEleveId(eleveId).stream()
                .filter(p -> p.getStatut() == StatutParentEleve.VALIDE && p.isPeutConsulter())
                .map(ParentEleve::getUtilisateurId)
                .distinct()
                .toList();
        if (destinataires.isEmpty()) {
            return 0;
        }
        List<Notification> creees = destinataires.stream().map(utilisateurId -> {
            Notification n = new Notification();
            n.setUtilisateurId(utilisateurId);
            n.setEtablissementId(etablissementId);
            n.setEleveId(eleveId);
            n.setType(type);
            n.setTitre(titre);
            n.setMessage(message);
            return n;
        }).toList();
        List<UUID> ids = notifications.saveAll(creees).stream().map(Notification::getId).toList();
        events.publishEvent(new NotificationsCreees(ids));
        return ids.size();
    }

    /** Vrai si une notification de ce type a déjà été émise pour cet élève depuis {@code depuis} (anti-doublon). */
    @Transactional(readOnly = true)
    public boolean dejaNotifie(UUID eleveId, TypeNotification type, Duration depuis) {
        return notifications.existsByEleveIdAndTypeAndCreatedAtAfter(eleveId, type, Instant.now().minus(depuis));
    }

    /** CU-SUR-07 : l'établissement envoie un message aux parents/tuteurs d'un élève. */
    @Transactional
    public EnvoiView envoyerMessage(UUID etablissementId, UUID eleveId, EnvoyerMessageRequest request) {
        if (!inscriptions.existsByEtablissementIdAndEleveId(etablissementId, eleveId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Élève introuvable pour cet établissement");
        }
        int n = notifierParents(etablissementId, eleveId, TypeNotification.MESSAGE_ETABLISSEMENT,
                request.titre().trim(), request.message().trim());
        if (n == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun parent/tuteur lié et validé pour cet élève");
        }
        return new EnvoiView(n);
    }

    /** CU-PAR-13 : notifications reçues par l'utilisateur connecté, les plus récentes d'abord. */
    @Transactional(readOnly = true)
    public NotificationsPage mesNotifications(UUID utilisateurId, boolean nonLuesSeulement, int page, int taille) {
        if (page < 0 || taille < 1 || taille > TAILLE_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "page doit être >= 0 et taille comprise entre 1 et " + TAILLE_MAX);
        }
        PageRequest pageable = PageRequest.of(page, taille);
        Page<Notification> resultat = nonLuesSeulement
                ? notifications.findByUtilisateurIdAndLueLeIsNullOrderByCreatedAtDesc(utilisateurId, pageable)
                : notifications.findByUtilisateurIdOrderByCreatedAtDesc(utilisateurId, pageable);
        return new NotificationsPage(resultat.map(NotificationView::from).getContent(), page, taille,
                resultat.getTotalElements(), notifications.countByUtilisateurIdAndLueLeIsNull(utilisateurId));
    }

    @Transactional(readOnly = true)
    public CompteurNonLuesView compterNonLues(UUID utilisateurId) {
        return new CompteurNonLuesView(notifications.countByUtilisateurIdAndLueLeIsNull(utilisateurId));
    }

    @Transactional
    public NotificationView marquerLue(UUID utilisateurId, UUID notificationId) {
        Notification n = notifications.findByIdAndUtilisateurId(notificationId, utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable"));
        if (n.getLueLe() == null) {
            n.setLueLe(Instant.now());
        }
        return NotificationView.from(n);
    }

    @Transactional
    public CompteurNonLuesView marquerToutesLues(UUID utilisateurId) {
        notifications.marquerToutesLues(utilisateurId, Instant.now());
        return new CompteurNonLuesView(0);
    }

    /** Envoi sur les canaux externes, seulement si la transaction ayant créé les notifications a été validée. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void envoyerSurCanauxExternes(NotificationsCreees event) {
        if (canauxExternes.isEmpty()) {
            log.debug("{} notification(s) créée(s), aucun canal externe configuré", event.ids().size());
            return;
        }
        List<Notification> aEnvoyer = notifications.findAllById(event.ids());
        Map<UUID, Utilisateur> destinataires = utilisateurs.findAllById(
                        aEnvoyer.stream().map(Notification::getUtilisateurId).distinct().toList())
                .stream().collect(Collectors.toMap(Utilisateur::getId, Function.identity()));
        for (Notification n : aEnvoyer) {
            Utilisateur destinataire = destinataires.get(n.getUtilisateurId());
            if (destinataire == null) {
                continue;
            }
            for (CanalNotificationExterne canal : canauxExternes) {
                try {
                    canal.envoyer(destinataire, n);
                } catch (RuntimeException e) {
                    log.warn("Échec d'envoi de la notification {} via {}", n.getId(), canal.getClass().getSimpleName(), e);
                }
            }
        }
    }
}
