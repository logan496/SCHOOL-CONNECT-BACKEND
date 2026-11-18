-- SchoolConnect : notifications aux parents/tuteurs (CU-SYS-01/03, CU-PAR-13, CU-SUR-07, relances CU-ECO-06).
-- Chaque ligne est une notification "in-app" pour un destinataire. L'envoi sur un canal externe
-- (SMS, e-mail, push) est délégué à un canal branchable côté application et n'est pas stocké ici.

CREATE TABLE notification (
                              id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                              utilisateur_id    UUID          NOT NULL REFERENCES utilisateur (id) ON DELETE CASCADE,
                              etablissement_id  UUID          REFERENCES etablissement (id) ON DELETE CASCADE,
                              eleve_id          UUID          REFERENCES eleve (id) ON DELETE CASCADE,
                              type              VARCHAR(30)   NOT NULL CHECK (type IN (
                                  'ABSENCE', 'RETARD', 'SORTIE_ANTICIPEE',
                                  'PAIEMENT_ENREGISTRE', 'PAIEMENT_ANNULE', 'RELANCE_IMPAYE',
                                  'MESSAGE_ETABLISSEMENT')),
                              titre             VARCHAR(150)  NOT NULL,
                              message           TEXT          NOT NULL,
                              lue_le            TIMESTAMPTZ,
                              created_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_notification_destinataire ON notification (utilisateur_id, created_at DESC);
CREATE INDEX idx_notification_non_lue ON notification (utilisateur_id) WHERE lue_le IS NULL;
-- Sert à l'anti-doublon des relances automatiques (dernière relance par élève).
CREATE INDEX idx_notification_eleve_type ON notification (eleve_id, type, created_at DESC);
