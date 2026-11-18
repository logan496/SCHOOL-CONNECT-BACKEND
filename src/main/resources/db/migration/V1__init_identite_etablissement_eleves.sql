-- SchoolConnect : socle identité, établissements, années scolaires, élèves, inscriptions, liens parents.
-- Conforme au dictionnaire de données (valeurs d'énumération en MAJUSCULES).

-- ---------------------------------------------------------------------------
-- Identité et accès (tables globales)
-- ---------------------------------------------------------------------------
CREATE TABLE utilisateur (
                             id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             nom                VARCHAR(100) NOT NULL,
                             prenom             VARCHAR(100) NOT NULL,
                             telephone          VARCHAR(20)  UNIQUE,
                             email              VARCHAR(150) UNIQUE,
                             mot_de_passe_hash  VARCHAR(255),
                             langue             VARCHAR(2)   NOT NULL DEFAULT 'fr' CHECK (langue IN ('fr', 'en')),
                             statut             VARCHAR(20)  NOT NULL DEFAULT 'ACTIF' CHECK (statut IN ('ACTIF', 'SUSPENDU', 'INVITE')),
                             derniere_connexion TIMESTAMPTZ,
                             created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
                             updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
                             CONSTRAINT utilisateur_contact_chk CHECK (telephone IS NOT NULL OR email IS NOT NULL)
);

-- ---------------------------------------------------------------------------
-- Établissement et paramétrage
-- ---------------------------------------------------------------------------
CREATE TABLE etablissement (
                               id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               nom                 VARCHAR(200) NOT NULL,
                               code                VARCHAR(30)  NOT NULL UNIQUE,
                               type                VARCHAR(30)  NOT NULL CHECK (type IN ('PUBLIC', 'PRIVE_LAIC', 'PRIVE_CONFESSIONNEL')),
                               sous_systeme        VARCHAR(20)  NOT NULL CHECK (sous_systeme IN ('FRANCOPHONE', 'ANGLOPHONE', 'BILINGUE')),
                               ordre_enseignement  VARCHAR(20)  NOT NULL CHECK (ordre_enseignement IN ('GENERAL', 'TECHNIQUE', 'LES_DEUX')),
                               region              VARCHAR(100),
                               departement         VARCHAR(100),
                               ville               VARCHAR(100),
                               adresse             VARCHAR(255),
                               telephone           VARCHAR(20),
                               email               VARCHAR(150),
                               logo_url            TEXT,
                               devise              VARCHAR(3)   NOT NULL DEFAULT 'XAF',
                               statut              VARCHAR(20)  NOT NULL DEFAULT 'ACTIF' CHECK (statut IN ('ACTIF', 'SUSPENDU', 'ARCHIVE')),
                               created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
                               updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Rôle d'un utilisateur dans un établissement (RBAC contextuel). etablissement_id est NULL pour SUPER_ADMIN.
CREATE TABLE role_utilisateur (
                                  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  utilisateur_id   UUID        NOT NULL REFERENCES utilisateur (id) ON DELETE CASCADE,
                                  etablissement_id UUID        REFERENCES etablissement (id) ON DELETE CASCADE,
                                  role             VARCHAR(20) NOT NULL CHECK (role IN ('SUPER_ADMIN', 'DIRECTION', 'SECRETARIAT', 'SURVEILLANT', 'ENSEIGNANT', 'ECONOMAT')),
                                  actif            BOOLEAN     NOT NULL DEFAULT TRUE,
                                  date_debut       DATE,
                                  date_fin         DATE,
                                  CONSTRAINT role_etablissement_chk CHECK (
                                      (role = 'SUPER_ADMIN' AND etablissement_id IS NULL) OR
                                      (role <> 'SUPER_ADMIN' AND etablissement_id IS NOT NULL)
                                      )
);
CREATE UNIQUE INDEX uq_role_utilisateur
    ON role_utilisateur (utilisateur_id, COALESCE(etablissement_id, '00000000-0000-0000-0000-000000000000'::uuid), role);
CREATE INDEX idx_role_utilisateur_etablissement ON role_utilisateur (etablissement_id);

CREATE TABLE annee_scolaire (
                                id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                etablissement_id UUID        NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
                                libelle          VARCHAR(20) NOT NULL,
                                date_debut       DATE        NOT NULL,
                                date_fin         DATE        NOT NULL,
                                courante         BOOLEAN     NOT NULL DEFAULT FALSE,
                                statut           VARCHAR(20) NOT NULL DEFAULT 'PREPARATION' CHECK (statut IN ('PREPARATION', 'EN_COURS', 'CLOTUREE')),
                                created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
                                CONSTRAINT annee_dates_chk CHECK (date_fin > date_debut),
                                CONSTRAINT uq_annee_etablissement_libelle UNIQUE (etablissement_id, libelle)
);
-- Une seule année courante par établissement
CREATE UNIQUE INDEX uq_annee_courante ON annee_scolaire (etablissement_id) WHERE courante;

CREATE TABLE periode (
                         id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         annee_scolaire_id UUID        NOT NULL REFERENCES annee_scolaire (id) ON DELETE CASCADE,
                         type              VARCHAR(20) NOT NULL CHECK (type IN ('SEQUENCE', 'TRIMESTRE', 'ANNUELLE')),
                         numero            INT         NOT NULL,
                         periode_parent_id UUID        REFERENCES periode (id),
                         date_debut        DATE        NOT NULL,
                         date_fin          DATE        NOT NULL,
                         saisie_ouverte    BOOLEAN     NOT NULL DEFAULT FALSE,
                         publiee           BOOLEAN     NOT NULL DEFAULT FALSE,
                         CONSTRAINT periode_dates_chk CHECK (date_fin > date_debut),
                         CONSTRAINT uq_periode UNIQUE (annee_scolaire_id, type, numero)
);

-- ---------------------------------------------------------------------------
-- Élèves (entité globale, non utilisateur), inscriptions, liens parents
-- ---------------------------------------------------------------------------
CREATE TABLE eleve (
                       id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       matricule_national VARCHAR(30) UNIQUE,   -- matricule unique MINESEC (cartescolaire.cm)
                       nom                VARCHAR(100) NOT NULL,
                       prenom             VARCHAR(100) NOT NULL,
                       sexe               VARCHAR(1)   NOT NULL CHECK (sexe IN ('M', 'F')),
                       date_naissance     DATE         NOT NULL,
                       lieu_naissance     VARCHAR(100),
                       nationalite        VARCHAR(50),
                       photo_url          TEXT,
                       infos_sante        TEXT,
                       created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_eleve_identite ON eleve (lower(nom), lower(prenom), date_naissance);

CREATE TABLE inscription (
                             id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             etablissement_id      UUID        NOT NULL REFERENCES etablissement (id),
                             eleve_id              UUID        NOT NULL REFERENCES eleve (id),
                             annee_scolaire_id     UUID        NOT NULL REFERENCES annee_scolaire (id),
                             classe_id             UUID,       -- FK ajoutée avec la table classe (migration suivante)
                             matricule             VARCHAR(30) NOT NULL,
                             type                  VARCHAR(20) NOT NULL CHECK (type IN ('NOUVEAU', 'REINSCRIPTION', 'TRANSFERT_ENTRANT')),
                             redoublant            BOOLEAN     NOT NULL DEFAULT FALSE,
                             etablissement_origine VARCHAR(200),
                             statut                VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE', 'VALIDE', 'TRANSFERE', 'SORTI', 'ABANDON')),
                             date_inscription      DATE        NOT NULL DEFAULT CURRENT_DATE,
                             date_sortie           DATE,
                             motif_sortie          TEXT,
                             created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
                             updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
                             CONSTRAINT uq_inscription_matricule UNIQUE (etablissement_id, matricule),
                             CONSTRAINT uq_inscription_eleve_annee UNIQUE (eleve_id, annee_scolaire_id, etablissement_id)
);
CREATE INDEX idx_inscription_eleve ON inscription (eleve_id);
CREATE INDEX idx_inscription_annee ON inscription (annee_scolaire_id);

-- Lien plusieurs-à-plusieurs entre un compte parent/tuteur et un enfant, avec droits distincts.
CREATE TABLE parent_eleve (
                              id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              utilisateur_id  UUID        NOT NULL REFERENCES utilisateur (id) ON DELETE CASCADE,
                              eleve_id        UUID        NOT NULL REFERENCES eleve (id) ON DELETE CASCADE,
                              lien            VARCHAR(20) NOT NULL CHECK (lien IN ('PERE', 'MERE', 'TUTEUR_LEGAL', 'AUTRE')),
                              principal       BOOLEAN     NOT NULL DEFAULT FALSE,
                              peut_consulter  BOOLEAN     NOT NULL DEFAULT TRUE,
                              peut_payer      BOOLEAN     NOT NULL DEFAULT FALSE,
                              peut_justifier  BOOLEAN     NOT NULL DEFAULT FALSE,
                              statut          VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE', 'VALIDE', 'REVOQUE')),
                              valide_par      UUID        REFERENCES utilisateur (id),
                              valide_le       TIMESTAMPTZ,
                              created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                              CONSTRAINT uq_parent_eleve UNIQUE (utilisateur_id, eleve_id)
);
CREATE INDEX idx_parent_eleve_eleve ON parent_eleve (eleve_id);
