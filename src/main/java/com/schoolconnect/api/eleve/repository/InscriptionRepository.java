package com.schoolconnect.api.eleve.repository;

import com.schoolconnect.api.eleve.model.Inscription;
import com.schoolconnect.api.eleve.model.StatutInscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscriptionRepository extends JpaRepository<Inscription, UUID> {

    List<Inscription> findByEtablissementIdAndAnneeScolaireIdOrderByMatricule(UUID etablissementId, UUID anneeScolaireId);

    Optional<Inscription> findByEleveIdAndEtablissementIdAndAnneeScolaireId(UUID eleveId, UUID etablissementId, UUID anneeScolaireId);

    List<Inscription> findByClasseIdAndAnneeScolaireIdAndStatut(UUID classeId, UUID anneeScolaireId, StatutInscription statut);

    Optional<Inscription> findFirstByEleveIdAndStatutOrderByDateInscriptionDesc(UUID eleveId, StatutInscription statut);

    boolean existsByEtablissementIdAndMatricule(UUID etablissementId, String matricule);

    boolean existsByEtablissementIdAndEleveId(UUID etablissementId, UUID eleveId);
}
