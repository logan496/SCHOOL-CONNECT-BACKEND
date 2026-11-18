-- SchoolConnect : grille tarifaire et enregistrement des paiements (CU-DIR-04, CU-ECO-01/02/03/06/07/08, CU-PAR-10/12).
-- Pas de passerelle de paiement en ligne : seul l'enregistrement d'un paiement déjà effectué
-- (espèces, Mobile Money, virement, chèque) est couvert, par l'économat.

CREATE TABLE frais_scolaire (
                                id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                                etablissement_id   UUID          NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
                                annee_scolaire_id  UUID          NOT NULL REFERENCES annee_scolaire (id) ON DELETE CASCADE,
                                niveau             VARCHAR(30)   NOT NULL,
                                libelle            VARCHAR(100)  NOT NULL,
                                montant            NUMERIC(12,2) NOT NULL CHECK (montant >= 0),
                                date_echeance      DATE          NOT NULL,
                                created_at         TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_frais_scolaire_niveau ON frais_scolaire (etablissement_id, annee_scolaire_id, niveau);

CREATE TABLE paiement (
                          id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                          etablissement_id   UUID          NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
                          eleve_id           UUID          NOT NULL REFERENCES eleve (id) ON DELETE CASCADE,
                          annee_scolaire_id  UUID          NOT NULL REFERENCES annee_scolaire (id) ON DELETE CASCADE,
                          montant            NUMERIC(12,2) NOT NULL CHECK (montant > 0),
                          mode               VARCHAR(20)   NOT NULL CHECK (mode IN ('ESPECES', 'MOBILE_MONEY', 'VIREMENT', 'CHEQUE')),
                          reference          VARCHAR(100),
                          date_paiement      DATE          NOT NULL,
                          numero_recu        VARCHAR(20)   NOT NULL UNIQUE,
                          statut             VARCHAR(20)   NOT NULL DEFAULT 'VALIDE' CHECK (statut IN ('VALIDE', 'ANNULE')),
                          enregistre_par     UUID          NOT NULL REFERENCES utilisateur (id),
                          motif_annulation   TEXT,
                          annule_par         UUID          REFERENCES utilisateur (id),
                          annule_le          TIMESTAMPTZ,
                          created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
                          updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_paiement_eleve ON paiement (eleve_id);
CREATE INDEX idx_paiement_etablissement_date ON paiement (etablissement_id, date_paiement);
