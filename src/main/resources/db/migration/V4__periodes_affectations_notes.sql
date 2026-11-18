-- SchoolConnect : affectation enseignant/classe/matière et notes (CU-ENS-02/04/05, CU-PAR-07).
-- La table "periode" existe depuis V1 ; ce lot lui ajoute seulement une consommation applicative.

CREATE TABLE affectation_enseignant (
                                         id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                         etablissement_id   UUID        NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
                                         annee_scolaire_id  UUID        NOT NULL REFERENCES annee_scolaire (id) ON DELETE CASCADE,
                                         classe_id          UUID        NOT NULL REFERENCES classe (id) ON DELETE CASCADE,
                                         matiere_id         UUID        NOT NULL REFERENCES matiere (id) ON DELETE CASCADE,
                                         enseignant_id      UUID        NOT NULL REFERENCES utilisateur (id) ON DELETE CASCADE,
                                         created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         CONSTRAINT uq_affectation UNIQUE (annee_scolaire_id, classe_id, matiere_id, enseignant_id)
);
CREATE INDEX idx_affectation_enseignant ON affectation_enseignant (enseignant_id);

CREATE TABLE evaluation (
                            id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                            etablissement_id  UUID         NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
                            classe_id         UUID         NOT NULL REFERENCES classe (id) ON DELETE CASCADE,
                            matiere_id        UUID         NOT NULL REFERENCES matiere (id) ON DELETE CASCADE,
                            periode_id        UUID         NOT NULL REFERENCES periode (id) ON DELETE CASCADE,
                            nom               VARCHAR(100) NOT NULL,
                            coefficient       NUMERIC(4,2) NOT NULL DEFAULT 1,
                            bareme            NUMERIC(5,2) NOT NULL DEFAULT 20,
                            date_evaluation   DATE         NOT NULL,
                            saisi_par         UUID         NOT NULL REFERENCES utilisateur (id),
                            created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
                            updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_evaluation_classe_periode ON evaluation (classe_id, periode_id, matiere_id);

CREATE TABLE note (
                      id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                      evaluation_id  UUID         NOT NULL REFERENCES evaluation (id) ON DELETE CASCADE,
                      eleve_id       UUID         NOT NULL REFERENCES eleve (id) ON DELETE CASCADE,
                      valeur         NUMERIC(5,2) CHECK (valeur IS NULL OR valeur >= 0),
                      absent         BOOLEAN      NOT NULL DEFAULT FALSE,
                      modifie_par    UUID         REFERENCES utilisateur (id),
                      created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
                      updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
                      CONSTRAINT uq_note_evaluation_eleve UNIQUE (evaluation_id, eleve_id)
);
CREATE INDEX idx_note_eleve ON note (eleve_id);
