package com.schoolconnect.api.paiement.service;

import com.schoolconnect.api.notification.model.TypeNotification;
import com.schoolconnect.api.notification.service.NotificationService;
import com.schoolconnect.api.paiement.dto.PaiementDtos.ImpayeView;
import com.schoolconnect.api.paiement.dto.PaiementDtos.RelanceView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Relance des parents/tuteurs pour les montants arrivés à échéance et non réglés
 * (CU-ECO-06 en manuel, CU-SYS-03 en automatique via {@link RelanceImpayesJob}).
 */
@Service
public class RelanceImpayesService {

    private final PaiementService paiements;
    private final NotificationService notifications;
    private final ZoneId fuseau;

    public RelanceImpayesService(PaiementService paiements, NotificationService notifications,
                                 @Value("${schoolconnect.fuseau-horaire:Africa/Douala}") String fuseau) {
        this.paiements = paiements;
        this.notifications = notifications;
        this.fuseau = ZoneId.of(fuseau);
    }

    /**
     * @param eleveIds        si non vide, restreint la relance à ces élèves (ceux sans impayé échu sont ignorés)
     * @param intervalleMin   si non nul, un élève déjà relancé depuis moins longtemps n'est pas relancé à nouveau
     */
    public RelanceView relancer(UUID etablissementId, Set<UUID> eleveIds, Duration intervalleMin) {
        List<ImpayeView> impayes = paiements.listImpayesEchus(etablissementId, LocalDate.now(fuseau)).stream()
                .filter(i -> eleveIds == null || eleveIds.isEmpty() || eleveIds.contains(i.eleveId()))
                .toList();

        int elevesRelances = 0;
        int notificationsEnvoyees = 0;
        int sansParent = 0;
        for (ImpayeView impaye : impayes) {
            if (intervalleMin != null
                    && notifications.dejaNotifie(impaye.eleveId(), TypeNotification.RELANCE_IMPAYE, intervalleMin)) {
                continue;
            }
            String eleve = impaye.eleve().prenom() + " " + impaye.eleve().nom();
            int n = notifications.notifierParents(etablissementId, impaye.eleveId(), TypeNotification.RELANCE_IMPAYE,
                    "Rappel de paiement pour " + eleve,
                    "Un montant de " + PaiementService.montant(impaye.solde()) + " est arrivé à échéance et reste à régler pour "
                            + eleve + " (dû à ce jour : " + PaiementService.montant(impaye.totalDu())
                            + ", déjà payé : " + PaiementService.montant(impaye.totalPaye())
                            + "). Merci de régulariser auprès de l'économat.");
            if (n == 0) {
                sansParent++;
            } else {
                elevesRelances++;
                notificationsEnvoyees += n;
            }
        }
        return new RelanceView(impayes.size(), elevesRelances, notificationsEnvoyees, sansParent);
    }
}
