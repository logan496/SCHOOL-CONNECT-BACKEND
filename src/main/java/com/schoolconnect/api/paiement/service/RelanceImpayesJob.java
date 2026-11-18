package com.schoolconnect.api.paiement.service;

import com.schoolconnect.api.paiement.dto.PaiementDtos.RelanceView;
import com.schoolconnect.api.school.model.Etablissement;
import com.schoolconnect.api.school.model.StatutEtablissement;
import com.schoolconnect.api.school.repository.EtablissementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * CU-SYS-03 : relance automatique des impayés échus, pour chaque établissement actif.
 * Un élève n'est relancé qu'une fois par intervalle (7 jours par défaut), même si le job
 * tourne tous les jours. Désactivable avec {@code schoolconnect.relance-impayes.enabled=false}.
 */
@Component
@ConditionalOnProperty(name = "schoolconnect.relance-impayes.enabled", havingValue = "true", matchIfMissing = true)
public class RelanceImpayesJob {

    private static final Logger log = LoggerFactory.getLogger(RelanceImpayesJob.class);

    private final EtablissementRepository etablissements;
    private final RelanceImpayesService relances;
    private final Duration intervalle;

    public RelanceImpayesJob(EtablissementRepository etablissements, RelanceImpayesService relances,
                             @Value("${schoolconnect.relance-impayes.intervalle-jours:7}") int intervalleJours) {
        this.etablissements = etablissements;
        this.relances = relances;
        this.intervalle = Duration.ofDays(intervalleJours);
    }

    @Scheduled(cron = "${schoolconnect.relance-impayes.cron:0 0 8 * * MON-FRI}",
            zone = "${schoolconnect.fuseau-horaire:Africa/Douala}")
    public void executer() {
        for (Etablissement etab : etablissements.findByStatut(StatutEtablissement.ACTIF)) {
            try {
                RelanceView r = relances.relancer(etab.getId(), null, intervalle);
                if (r.elevesRelances() > 0 || r.elevesSansParentLie() > 0) {
                    log.info("Relance impayés {} : {} élève(s) en impayé, {} relancé(s), {} notification(s), {} sans parent lié",
                            etab.getId(), r.elevesEnImpaye(), r.elevesRelances(), r.notificationsEnvoyees(), r.elevesSansParentLie());
                }
            } catch (RuntimeException e) {
                // Typiquement : pas d'année scolaire courante. Un établissement en erreur ne bloque pas les autres.
                log.warn("Relance impayés ignorée pour l'établissement {} : {}", etab.getId(), e.getMessage());
            }
        }
    }
}
