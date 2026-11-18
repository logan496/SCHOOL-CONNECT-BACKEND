-- SchoolConnect : appel de classe et présences (CU-ENS-03, CU-SUR-01/02/04, CU-PAR-05).

CREATE TABLE appel (
                        id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                        etablissement_id   UUID        NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
                        classe_id          UUID        NOT NULL REFERENCES classe (id) ON DELETE CASCADE,
                        annee_scolaire_id  UUID        NOT NULL REFERENCES annee_scolaire (id) ON DELETE CASCADE,
                        date_appel         DATE        NOT NULL,
                        matiere_id         UUID        REFERENCES matiere (id),
                        saisi_par          UUID        NOT NULL REFERENCES utilisateur (id),
                        created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                        CONSTRAINT uq_appel_classe_date UNIQUE (classe_id, date_appel)
);
CREATE INDEX idx_appel_etablissement_date ON appel (etablissement_id, date_appel);

-- Une ligne par élève inscrit dans la classe au moment de l'appel ; PRESENT par défaut,
-- seules les exceptions (absent, retard, sortie anticipée) sont saisies explicitement.
CREATE TABLE presence_eleve (
                                id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                appel_id      UUID        NOT NULL REFERENCES appel (id) ON DELETE CASCADE,
                                eleve_id      UUID        NOT NULL REFERENCES eleve (id) ON DELETE CASCADE,
                                statut        VARCHAR(20) NOT NULL CHECK (statut IN ('PRESENT', 'ABSENT', 'RETARD', 'SORTIE_ANTICIPEE')),
                                heure_arrivee TIME,
                                heure_sortie  TIME,
                                created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                CONSTRAINT uq_presence_appel_eleve UNIQUE (appel_id, eleve_id)
);
CREATE INDEX idx_presence_eleve_eleve ON presence_eleve (eleve_id);
