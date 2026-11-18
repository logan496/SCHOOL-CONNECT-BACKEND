-- SchoolConnect : classes, matières et flux secrétariat (tuteurs, codes de liaison).

CREATE TABLE classe (
                         id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         etablissement_id   UUID        NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
                         annee_scolaire_id  UUID        NOT NULL REFERENCES annee_scolaire (id) ON DELETE CASCADE,
                         nom                VARCHAR(50) NOT NULL,
                         niveau             VARCHAR(30) NOT NULL,
                         effectif_max       INT,
                         created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                         CONSTRAINT uq_classe_annee_nom UNIQUE (annee_scolaire_id, nom)
);
CREATE INDEX idx_classe_etablissement ON classe (etablissement_id);

-- Complète la FK laissée en attente par la migration précédente (classe créée après inscription).
ALTER TABLE inscription
    ADD CONSTRAINT fk_inscription_classe FOREIGN KEY (classe_id) REFERENCES classe (id);

CREATE TABLE matiere (
                         id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         etablissement_id    UUID         NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
                         nom                 VARCHAR(100) NOT NULL,
                         code                VARCHAR(20)  NOT NULL,
                         coefficient_defaut  NUMERIC(4,2) NOT NULL DEFAULT 1,
                         created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
                         CONSTRAINT uq_matiere_etablissement_code UNIQUE (etablissement_id, code)
);

-- Code remis par le secrétariat à un tuteur qui n'a pas encore de compte, pour qu'il se rattache
-- lui-même à l'élève plus tard (CU-SEC-04). La consommation du code côté parent (CU-PAR-02) n'est
-- pas encore implémentée ; cette table pose seulement les fondations.
CREATE TABLE code_liaison (
                              id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                              eleve_id          UUID        NOT NULL REFERENCES eleve (id) ON DELETE CASCADE,
                              etablissement_id  UUID        NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
                              code              VARCHAR(10) NOT NULL UNIQUE,
                              lien              VARCHAR(20) NOT NULL CHECK (lien IN ('PERE', 'MERE', 'TUTEUR_LEGAL', 'AUTRE')),
                              cree_par          UUID        NOT NULL REFERENCES utilisateur (id),
                              expire_le         TIMESTAMPTZ NOT NULL,
                              utilise           BOOLEAN     NOT NULL DEFAULT FALSE,
                              utilise_par       UUID        REFERENCES utilisateur (id),
                              utilise_le        TIMESTAMPTZ,
                              created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_code_liaison_eleve ON code_liaison (eleve_id);
