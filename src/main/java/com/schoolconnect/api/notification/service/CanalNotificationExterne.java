package com.schoolconnect.api.notification.service;

import com.schoolconnect.api.identity.model.Utilisateur;
import com.schoolconnect.api.notification.model.Notification;

/**
 * Canal d'envoi hors application (SMS, e-mail, push). Appelé après la validation de la
 * transaction qui a créé la notification ; un échec d'envoi ne remet pas en cause la
 * notification in-app. Pour brancher un fournisseur réel, déclarer un bean qui implémente
 * cette interface : plusieurs canaux peuvent coexister. Sans aucun canal déclaré, seule la notification in-app est produite.
 */
public interface CanalNotificationExterne {

    void envoyer(Utilisateur destinataire, Notification notification);
}
