# Analyse des écarts — projet actuel vs cahier des charges / cas d'utilisation

Comparaison entre le code présent dans ce dépôt (backend Spring Boot) et les deux documents
de référence du groupe 5 (UCAC-ICAM) :
- `CAHIER_DE_CHARGE_SCHOOL_CONNECT.pdf`
- `cas-utilisation-smart-realization.pdf`

État initial : 18/11/2026. Mis à jour le 18/11/2026 après implémentation du module
« année scolaire / classes / matières / secrétariat » (voir section 6), puis après
réorganisation du code en couches et implémentation du module « présences » (section 7).

## 1. Vue d'ensemble

Le projet actuel est un squelette backend qui couvre uniquement le **socle identité /
établissement**. Aucun module pédagogique, financier ou de communication n'est encore
implémenté. Sur les 8 acteurs listés dans le document de cas d'utilisation, seuls des
fragments des acteurs **Parent/Tuteur**, **Secrétariat**, **Direction** et **Super-admin**
existent, et uniquement côté inscription/authentification/paramétrage établissement.

| Domaine du cahier des charges (MVP) | État |
|---|---|
| Paramétrage établissement (année scolaire, périodes, classes, matières) | **Partiel** — établissement, année scolaire, période en base ; classes/matières absentes |
| Gestion des élèves (inscription, réinscription, dossier, affectation classe) | **Partiel** — schéma SQL `eleve`/`inscription` présent, aucun service/contrôleur |
| Gestion du personnel enseignant et administratif | **Absent** — seul `role_utilisateur` existe (RBAC générique) |
| Présences et absences (élèves et personnel) | **Absent** |
| Notes, moyennes, rangs, bulletins PDF | **Absent** |
| Paiements, grille tarifaire, reçus, impayés | **Absent** |
| Notifications aux parents (push/SMS/email) | **Absent** |
| Espaces dédiés par profil | **Partiel** — RBAC (rôles + portée établissement) en place dans le JWT, pas d'espaces/API par profil |
| Tableaux de bord et exports | **Absent** |

## 2. Couverture détaillée par cas d'utilisation

Légende : ✅ fait · 🟡 partiel (schéma seul, ou fonctionnalité minimale) · ❌ absent

### 2.1 Parent/Tuteur
| ID | Cas d'utilisation | Prio | État |
|---|---|---|---|
| CU-PAR-01 | Créer un compte / se connecter | M | ✅ `AuthController` (register/login, téléphone ou email, JWT) |
| CU-PAR-02 | Lier un enfant via code/invitation | M | 🟡 table `parent_eleve` en base, aucun endpoint |
| CU-PAR-03 | Basculer entre enfants/établissements, vue consolidée | M | ❌ |
| CU-PAR-04 | Consulter la fiche enfant | M | ❌ |
| CU-PAR-05 | Consulter présences/absences/retards | M | ✅ `PresenceEleveController.listPourParent` (`GET /mes-enfants/{id}/presences`), limité aux liens `VALIDE` avec `peutConsulter` |
| CU-PAR-06 | Justifier une absence | S | ❌ |
| CU-PAR-07 | Consulter les notes | M | ✅ `NoteEleveController.listPourParent` (`GET /mes-enfants/{id}/notes`), par matière et par période, avec moyenne calculée |
| CU-PAR-08 | Consulter/télécharger bulletins | M | ❌ |
| CU-PAR-09 | Consulter emploi du temps | S | ❌ |
| CU-PAR-10 | Consulter situation financière | M | ✅ `SituationFinanciereParentController` (`GET /mes-enfants/{id}/situation-financiere`) : dû, payé, solde |
| CU-PAR-11 | Payer en ligne / déclarer un paiement | M | ❌ (paiement en ligne hors MVP par choix du cahier des charges ; « déclarer un paiement » — un parent signale lui-même un paiement effectué — non implémenté, seul l'économat enregistre) |
| CU-PAR-12 | Télécharger reçus/historique | M | 🟡 historique des paiements inclus dans la situation financière (JSON) ; pas de téléchargement PDF du reçu |
| CU-PAR-13 | Recevoir notifications | M | ❌ |
| CU-PAR-14 | Consulter annonces/convocations | M | ❌ |
| CU-PAR-15 | Échanger avec l'établissement | C | ❌ (hors MVP) |
| CU-PAR-16 | Gérer profil/préférences notif | S | 🟡 `langue` sur `Utilisateur`, pas de préférences notif |
| CU-PAR-17 | Se dissocier d'un enfant / correction | S | ❌ |

### 2.2 Direction
| ID | Cas d'utilisation | Prio | État |
|---|---|---|---|
| CU-DIR-01 | Tableau de bord | M | ❌ |
| CU-DIR-02 | Paramétrer année scolaire/périodes/classes | M | ✅ année scolaire, classes, matières et périodes exposées (`AnneeScolaireController`, `ClasseController`, `MatiereController`, `PeriodeController`), y compris ouverture/fermeture de la saisie des notes |
| CU-DIR-03 | Gérer utilisateurs et rôles | M | 🟡 `role_utilisateur` en base + autorités JWT, aucun endpoint d'attribution |
| CU-DIR-04 | Grille tarifaire et échéanciers | M | ✅ `FraisScolaireController` — une ligne par niveau/libellé/montant/échéance |
| CU-DIR-05 | Valider résultats / publier bulletins | M | ❌ |
| CU-DIR-06 | Assiduité enseignants/personnel | M | ❌ |
| CU-DIR-07 | Rapports financiers/impayés | M | ✅ `RapportFinancierController.impayes` (solde agrégé, pas d'échéance individuelle) |
| CU-DIR-08 | Publier annonces | S | ❌ |
| CU-DIR-09 | Exports statistiques | S | ❌ |
| CU-DIR-10 | Journal d'audit | S | ❌ (pas de table d'audit) |

### 2.3 Secrétariat/Scolarité
| ID | Cas d'utilisation | Prio | État |
|---|---|---|---|
| CU-SEC-01 | Inscrire un nouvel élève | M | ✅ `EleveController.inscrire` (`POST /etablissements/{id}/eleves`) |
| CU-SEC-02 | Réinscrire un élève | M | ❌ (pas de distinction par rapport à une nouvelle inscription) |
| CU-SEC-03 | Affecter/transférer en classe | M | ✅ `EleveController.affecterClasse` (`PATCH .../eleves/{id}/classe`), basé sur l'année scolaire courante |
| CU-SEC-04 | Enregistrer tuteurs + code de liaison | M | ✅ `EleveController.enregistrerTuteur` — lie directement si le tuteur a déjà un compte, sinon génère un code de liaison à 8 caractères (valable 30 jours) |
| CU-SEC-05 | Valider/rejeter liaison parent-enfant | M | ✅ `EleveController.validerLiaison` / `rejeterLiaison`, limité aux demandes `EN_ATTENTE` |
| CU-SEC-06 | Corriger dossier avec historique | M | ❌ (pas de table d'historique) |
| CU-SEC-07 | Import en masse Excel/CSV | S | ❌ |
| CU-SEC-08 | Générer documents (certificat, carte) | S | ❌ |
| CU-SEC-09 | Gérer dossiers du personnel | M | ❌ |
| CU-SEC-10 | Archiver dossier élève sorti | S | 🟡 `inscription.statut = SORTI` possible en base, pas de flux |

### 2.4 Surveillant général
| ID | Cas d'utilisation | Prio | État |
|---|---|---|---|
| CU-SUR-01 | Consulter les présences par classe et par jour | M | ✅ `AppelController.getParDate` |
| CU-SUR-02 | Saisir ou corriger l'appel quand l'enseignant ne l'a pas fait | M | ✅ `AppelController.saisir`/`corriger` (rôle SURVEILLANT autorisé) |
| CU-SUR-03 | Valider les justificatifs d'absence soumis par les parents | M | ❌ (pas de workflow de justification) |
| CU-SUR-04 | Enregistrer les retards et sorties anticipées | M | ✅ via `corriger` (statuts RETARD/SORTIE_ANTICIPEE + heures) |
| CU-SUR-05 | Enregistrer l'assiduité des enseignants et du personnel | M | ❌ (le modèle actuel ne couvre que les élèves) |
| CU-SUR-06 | Enregistrer sanctions et avertissements | C | ❌ |
| CU-SUR-07 | Déclencher ou relancer des notifications aux parents | S | ❌ |
| CU-SUR-08 | Éditer les statistiques d'assiduité | S | ❌ |

### 2.5 Enseignant
| ID | Cas d'utilisation | Prio | État |
|---|---|---|---|
| CU-ENS-01 | Se connecter et choisir son établissement | M | 🟡 connexion couverte par CU-PAR-01 (même endpoint) ; pas de sélecteur multi-établissement dédié |
| CU-ENS-02 | Consulter ses classes, matières et emploi du temps | M | 🟡 `AffectationEnseignantController.mesAffectations` couvre classes/matières ; pas d'emploi du temps |
| CU-ENS-03 | Faire l'appel d'une classe | M | ✅ `AppelController.saisir` |
| CU-ENS-04 | Saisir les notes d'une évaluation | M | ✅ `EvaluationController.creer` |
| CU-ENS-05 | Corriger une note | M | ✅ `EvaluationController.corriger`, limité à la période ouverte (`periode.saisie_ouverte`) |
| CU-ENS-06 à 10 | Appréciations, statistiques, cahier de textes, messagerie | S/C | ❌ |

### 2.6 Comptable/Économat
| ID | Cas d'utilisation | Prio | État |
|---|---|---|---|
| CU-ECO-01 | Consulter la situation financière d'un élève | M | ✅ `PaiementController.situation` |
| CU-ECO-02 | Enregistrer un paiement (espèces, Mobile Money, virement) | M | ✅ `PaiementController.enregistrer` (+ chèque) |
| CU-ECO-03 | Émettre un reçu numérique | M | 🟡 numéro de reçu généré et consultable (JSON), pas d'impression/envoi PDF |
| CU-ECO-04 | Remises, bourses, échéanciers personnalisés | S | ❌ (grille tarifaire uniforme par niveau) |
| CU-ECO-05 | Rapprocher les paiements en ligne avec la comptabilité | S | ❌ (hors sujet, pas de paiement en ligne) |
| CU-ECO-06 | Suivre les impayés et envoyer des relances | M | 🟡 `RapportFinancierController.impayes` liste les soldes positifs ; pas d'envoi de relance (notifications) |
| CU-ECO-07 | Annuler ou corriger un paiement (justification + validation) | M | ✅ `PaiementController.annuler`, motif obligatoire, tracé (`annule_par`/`annule_le`) |
| CU-ECO-08 | Éditer les rapports de caisse journaliers/périodiques | M | 🟡 `RapportFinancierController.rapportCaisse` journalier uniquement, pas de rapport périodique agrégé |
| CU-ECO-09 | Exporter les données financières | S | ❌ |

### 2.7 Super-admin
| ID | Cas d'utilisation | Prio | État |
|---|---|---|---|
| CU-SAD-01 | Créer/configurer un établissement | M | ✅ `EtablissementController.create` (`@PreAuthorize("hasAuthority('SUPER_ADMIN')")`) |
| CU-SAD-02 | Créer le premier admin de l'établissement | M | 🟡 `BootstrapAdminRunner` ne crée que le **super-admin global** via variables d'env ; aucun flux pour créer le premier admin **d'un établissement donné** |
| CU-SAD-03 | Abonnements, activation/suspension | S | 🟡 `etablissement.statut` (ACTIF/SUSPENDU/ARCHIVE) existe en base, pas d'endpoint |
| CU-SAD-04 | Supervision usage/santé plateforme | S | ❌ (actuator/health exposé mais pas de supervision applicative) |
| CU-SAD-05 | Support tracé | S | ❌ |
| CU-SAD-06 | Comptes en doublon | S | ❌ |

### 2.8 Acteur système — ❌ entièrement absent (notifications, calculs auto, rappels, sauvegarde/audit)

## 3. Écarts par rapport au cahier des charges (hors cas d'utilisation)

- **Multi-tenant** : le principe (`etablissement_id` scopé, `SUPER_ADMIN` sans établissement)
  est posé dans le schéma et dans les autorités JWT (`ETAB:<id>`), mais rien ne vérifie encore
  l'isolation des données côté élèves/notes/paiements puisque ces modules n'existent pas.
- **RBAC** : conforme à l'esprit du glossaire (rôles par établissement), mais limité pour
  l'instant à `SUPER_ADMIN` et à la lecture d'établissement — les 5 autres rôles métier
  (DIRECTION, SECRETARIAT, SURVEILLANT, ENSEIGNANT, ECONOMAT) n'ont aucune route protégée.
- **Bilingue (fr/en)** : seul un champ `langue` sur `Utilisateur` existe ; aucune
  internationalisation des réponses API/messages d'erreur (tous les messages sont en français
  en dur, ex. `AuthService`, `EtablissementService`).
- **Mobile Money / paiements** : complètement hors scope du code actuel alors que c'est un
  objectif mesurable explicite du cahier des charges (« Part des paiements enregistrés avec
  reçu numérique : 100% »).
- **Connectivité irrégulière / mode dégradé** : aucune considération (cache, retry, file
  d'attente) dans le code actuel — cohérent avec le fait que c'est seulement un backend API
  pour l'instant, mais à garder en tête pour le client mobile à venir.
- **Objectifs mesurables** du cahier des charges (délai de notification < 1h, délai de
  détection impayé < 3 jours, réduction de 90% du délai de bulletin) ne sont pas mesurables
  aujourd'hui puisque les modules correspondants (présences, paiements, notes) n'existent pas.

## 4. Ce qui est fait et conforme

- Authentification (inscription/connexion parent par téléphone **ou** email, JWT stateless,
  BCrypt) correspond à CU-PAR-01.
- Modèle RBAC contextualisé par établissement (`role_utilisateur`, contrainte SQL forçant
  `SUPER_ADMIN` sans établissement) correspond au glossaire RBAC + multi-tenant.
  et à CU-SAD-01/02 en partie.
- Création d'établissement réservée au super-admin correspond à CU-SAD-01.
- Le schéma SQL anticipe correctement plusieurs cas d'usage non encore exposés en API :
  `annee_scolaire`/`periode` (séquence/trimestre, cohérent avec le glossaire),
  `parent_eleve` (droits distincts voir/payer/justifier — conforme à la règle de gestion
  « droits d'un tuteur définis par enfant »), `inscription` (types NOUVEAU/REINSCRIPTION/
  TRANSFERT_ENTRANT, statut SORTI/ABANDON).

## 5. Priorités suggérées pour la suite (cas M manquants, dans un ordre logique de dépendances)

1. ~~**Classes et matières**~~ — fait, voir section 6.
2. ~~**Secrétariat** (CU-SEC-01, 03, 04, 05)~~ — fait, voir section 6.
3. ~~**Présences** (CU-ENS-03, CU-SUR-01/02/04, CU-PAR-05)~~ — fait, voir section 7. Reste
   hors scope : la notification d'absence elle-même (objectif mesurable « < 1h », dépend du
   module notifications système) et l'assiduité du personnel (CU-SUR-05).
4. ~~**Notes** (CU-ENS-02/04/05, CU-PAR-07)~~ — fait, voir section 8. Reste : **bulletins**
   (CU-DIR-05 validation/publication, CU-PAR-08 PDF) — nécessite moyenne générale + rang par
   classe (pas seulement par matière) et génération de PDF, volontairement laissé de côté.
5. ~~**Paiements** (CU-ECO-01/02/03/06/07/08, CU-PAR-10/12, CU-DIR-04/07)~~ — fait, voir
   section 9. Pas de paiement en ligne (choix du cahier des charges), pas de reçu PDF, pas de
   relances automatiques (dépend du module notifications).
6. **Notifications système** (CU-SYS-01/03) — transverse au module 5 et à l'absence (module 3).
7. **Tableaux de bord direction** (CU-DIR-01) une fois le module 6 posé, et **bulletins**
   (voir point 4).

Restent aussi en attente, non bloquants pour la suite ci-dessus : CU-SEC-02 (réinscription),
CU-SEC-06 (historique de correction de dossier), CU-PAR-02 (le parent consomme lui-même le
code de liaison généré par CU-SEC-04 — endpoint encore à écrire côté parent), CU-DIR-03
(attribution de rôles via API, le modèle RBAC existe déjà), CU-SUR-03/CU-PAR-06 (justification
d'absence — les colonnes nécessaires n'existent pas encore sur `presence_eleve`), CU-SUR-05
(assiduité du personnel).

## 6. Détail de ce qui vient d'être implémenté (module classes/matières + secrétariat)

- `AnneeScolaireController`/`Service` : `POST`/`GET /api/v1/etablissements/{id}/annees-scolaires`
  — paramétrage minimal nécessaire pour que les classes et inscriptions aient une année
  scolaire valide (une seule `courante` par établissement, imposé par le service).
- `ClasseController`/`Service` et `MatiereController`/`Service` : CRUD de base (création +
  liste) sous `/etablissements/{id}/classes` et `/etablissements/{id}/matieres`.
- Migration `V2__classes_matieres_secretariat.sql` : tables `classe`, `matiere`,
  `code_liaison`, plus la FK `inscription.classe_id → classe.id` laissée en attente par `V1`.
- `EleveController`/`Service` (package `eleve`, nouveau) :
  - inscription élève + dossier (`Eleve` + `Inscription`) avec vérification d'unicité du
    matricule par établissement ;
  - affectation en classe, résolue via l'année scolaire `courante` de l'établissement ;
  - enregistrement tuteur : lien direct et validé si le tuteur a déjà un compte
    (`utilisateur` trouvé par téléphone/email), sinon génération d'un `code_liaison` à
    consommer plus tard (CU-PAR-02, non implémenté) ;
  - validation/rejet d'une demande de liaison `parent_eleve` en statut `EN_ATTENTE`.
- `PhoneNumbers` (package `identity`) rendue publique pour être réutilisée par le module
  `eleve` plutôt que dupliquée.
- Toutes les routes sont restreintes par établissement via les autorités JWT
  (`SUPER_ADMIN`, ou `DIRECTION:<id>` / `SECRETARIAT:<id>` selon l'endpoint).
- Correction connexe : `AuthDtos` était un stub vide qui empêchait déjà la compilation du
  projet avant ce travail (`AuthController`/`AuthService` référençaient des records
  inexistants) ; complété avec `RegisterRequest`, `LoginRequest`, `AuthResponse`, `UserView`,
  `RoleView`.

## 7. Réorganisation en couches + module présences

### Réorganisation architecturale

Chaque package de fonctionnalité (`identity`, `school`, `eleve`) est désormais scindé en
sous-packages par couche plutôt que d'avoir tous les fichiers à plat :
- `model/` — entités JPA et enums
- `repository/` — interfaces Spring Data
- `service/` — logique métier
- `dto/` — records de requête/réponse (classes `XxxDtos`, inchangées sinon)
- `web/` — contrôleurs REST

Les packages transverses (`security`, `config`, `common`) restent plats : ils ne suivent pas
ce découpage métier. `PhoneNumbers` (utilitaire) vit dans `identity/service/`.

Risque pris en compte : le dépôt n'avait pas de contrôle de version avant cette session. Un
`git init` + commit de référence a été fait avant tout déplacement de fichier, pour pouvoir
revenir en arrière en cas de problème.

### Module présences (CU-ENS-03, CU-SUR-01/02/04, CU-PAR-05)

Nouveau package `presence`, construit directement selon l'architecture en couches ci-dessus.

- Migration `V3__presences.sql` : tables `appel` (une ligne par classe et par jour, unique
  sur `classe_id, date_appel`) et `presence_eleve` (une ligne par élève inscrit à cette classe
  pour cet appel, statut `PRESENT`/`ABSENT`/`RETARD`/`SORTIE_ANTICIPEE`).
- `AppelService.saisirAppel` (CU-ENS-03, et CU-SUR-02 pour le cas « l'enseignant ne l'a pas
  fait ») : ne demande que les exceptions à la requête — tout élève du registre de la classe
  non mentionné est marqué `PRESENT` par défaut. Rejette la création si un appel existe déjà
  pour cette classe à cette date (redirige vers la correction).
- `AppelService.corrigerAppel` (CU-SUR-02 correction, CU-SUR-04 retards/sorties anticipées) :
  met à jour des lignes `presence_eleve` déjà existantes, avec heure d'arrivée/sortie.
- `AppelService.getParClasseEtDate` (CU-SUR-01) : lecture d'un appel par classe + jour.
- `AppelService.listPourEleve` / `listPourParent` : historique des présences d'un élève,
  respectivement pour le personnel de l'établissement et pour un parent — ce dernier n'est
  pas autorisé par rôle d'établissement (le parent n'en a pas) mais par vérification, dans le
  service, du lien `parent_eleve` (`statut = VALIDE` et `peutConsulter = true`). Endpoint :
  `GET /api/v1/mes-enfants/{eleveId}/presences`.
- Routes de saisie/correction ouvertes aux rôles `ENSEIGNANT`, `SURVEILLANT`, `DIRECTION` et
  `SUPER_ADMIN` d'un établissement (pas de table d'affectation enseignant↔classe pour
  restreindre plus finement : tout enseignant de l'établissement peut aujourd'hui faire
  l'appel de n'importe quelle classe — acceptable pour le MVP, à resserrer si CU-ENS-02 est
  implémenté).

Non couvert par ce lot (voir section 5) : notification automatique des parents en cas
d'absence (CU-SYS-01, nécessite le module notifications), justification d'absence côté parent
et validation côté surveillant (CU-PAR-06, CU-SUR-03), assiduité du personnel (CU-SUR-05).

## 8. Module notes (CU-ENS-02/04/05, CU-PAR-07)

Deux prérequis manquants ont été ajoutés dans `school` avant le module notes lui-même :

- **Périodes** (`PeriodeController`, `/annees-scolaires/{id}/periodes`) : la table `periode`
  existait depuis `V1` mais sans service ni contrôleur. Ajouté avec un verrou explicite
  `saisie_ouverte` (ouvert/fermé par `PATCH .../periodes/{id}/ouvrir|fermer`, réservé à
  `DIRECTION`/`SUPER_ADMIN`) : CU-ENS-04/05 vérifient ce verrou avant toute écriture.
- **Affectation enseignant↔classe/matière** (`AffectationEnseignantController`,
  `/etablissements/{id}/affectations` pour la direction, `/mes-affectations` pour
  l'enseignant lui-même — CU-ENS-02). Vérifie que l'utilisateur affecté a bien le rôle
  `ENSEIGNANT` dans l'établissement avant de créer l'affectation.

Nouveau package `note` (architecture en couches, comme `presence`) :

- Migration `V4__periodes_affectations_notes.sql` : tables `affectation_enseignant`,
  `evaluation`, `note`.
- `NoteService.creerEvaluation` (CU-ENS-04) : crée une évaluation (nom, coefficient, barème,
  période, date) et les notes initiales en une seule requête. Les élèves omis restent
  simplement sans note (distinct d'une note à 0), à compléter via la correction.
- `NoteService.corrigerNotes` (CU-ENS-05) : met à jour des notes existantes ou en ajoute pour
  les élèves omis à la création — la « correction » et la « saisie tardive » partagent le même
  endpoint. Trace l'auteur de la dernière modification (`note.modifie_par`), mais sans
  historique complet des valeurs précédentes (pas de table d'audit dédiée, cohérent avec le
  reste du projet qui n'en a pas non plus).
- Autorisation : `DIRECTION`/`SUPER_ADMIN` peuvent agir sur toute classe/matière de
  l'établissement ; un `ENSEIGNANT` doit avoir une `affectation_enseignant` correspondante,
  vérifiée en base (pas seulement par rôle générique, contrairement au module présences).
- `NoteService.listPourEleve` / `listPourParent` (CU-PAR-07) : regroupe les notes d'un élève
  par matière pour une période donnée, avec calcul de la moyenne pondérée par les coefficients
  (notes ramenées sur 20 selon le barème de chaque évaluation). Même pattern d'autorisation
  parent que le module présences (lien `parent_eleve` validé + `peutConsulter`), endpoint
  `GET /api/v1/mes-enfants/{eleveId}/notes?periodeId=&matiereId=`.

Non couvert (voir section 5) : moyenne générale tous matières confondues, rang dans la classe,
validation/publication des bulletins (CU-DIR-05), export PDF du bulletin (CU-PAR-08).

## 9. Module paiements (CU-DIR-04, CU-ECO-01/02/03/06/07/08, CU-PAR-10/12)

Pas de passerelle de paiement en ligne (Mobile Money direct) : conformément au cahier des
charges, c'est prévu pour une version ultérieure. Ce lot couvre uniquement
**l'enregistrement** de paiements déjà effectués par ailleurs.

Nouveau package `paiement` (architecture en couches, comme `presence` et `note`) :

- Migration `V5__paiements.sql` : tables `frais_scolaire` (grille tarifaire) et `paiement`.
- `FraisScolaireController` (CU-DIR-04) : une ligne de grille tarifaire = niveau + libellé +
  montant + date d'échéance, pour une année scolaire. Pas d'échéancier personnalisé par élève
  (CU-ECO-04, S) : tous les élèves d'un même niveau ont les mêmes frais.
- `PaiementService.enregistrerPaiement` (CU-ECO-02) : espèces, Mobile Money (déclaré, pas
  prélevé), virement ou chèque. Génère un numéro de reçu unique (CU-ECO-03) ; pas de PDF
  imprimable, le reçu reste consultable en JSON via la situation financière.
- `PaiementService.annulerPaiement` (CU-ECO-07) : motif obligatoire, pas de suppression —
  passe le paiement en statut `ANNULE` et trace qui/quand (`annule_par`/`annule_le`).
- `PaiementService.calculerSituation` (CU-ECO-01, CU-PAR-10/12) : calcule à la volée
  `totalDu` (somme des `frais_scolaire` du niveau de la classe courante de l'élève),
  `totalPaye` (somme des paiements `VALIDE`) et `solde` ; pas de table d'échéances par élève
  (pas de snapshot à l'inscription), donc un changement de grille tarifaire en cours d'année
  affecte rétroactivement le montant dû affiché — acceptable pour le MVP, à corriger si cela
  devient un problème réel (snapshotter les frais à l'inscription plutôt que les recalculer).
- `RapportFinancierController.impayes` (CU-ECO-06, CU-DIR-07) : élèves à solde positif pour
  l'année courante, triés par solde décroissant. Pas de détection par échéance individuelle en
  retard (juste le solde agrégé), et pas d'envoi de relance (dépend du module notifications).
- `RapportFinancierController.rapportCaisse` (CU-ECO-08) : total encaissé un jour donné, par
  mode de paiement. Pas de rapport périodique (hebdo/mensuel) agrégé — à construire en
  répétant l'appel par date si besoin, ou en ajoutant un endpoint dédié plus tard.
- Autorisation : `ECONOMAT`/`DIRECTION`/`SUPER_ADMIN` pour enregistrer/annuler/consulter les
  rapports ; lecture de la situation financière d'un élève ouverte à tout rôle d'établissement
  (`ETAB:`) ; côté parent, même pattern que présences/notes (lien `parent_eleve` validé +
  `peutConsulter` — `peutPayer` n'est pas encore utilisé puisqu'il n'y a pas de paiement en
  ligne à autoriser).

Non couvert (voir section 5) : remises/bourses (CU-ECO-04), rapprochement paiements en ligne
(CU-ECO-05, sans objet), export de données (CU-ECO-09), reçu PDF, relances automatiques.
