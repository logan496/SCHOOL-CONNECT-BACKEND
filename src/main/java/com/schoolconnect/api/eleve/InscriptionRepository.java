package com.schoolconnect.api.eleve;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InscriptionRepository extends JpaRepository<Inscription, UUID> {

    List<Inscription> findByEtablissementIdAndAnneeScolaireIdOrderByMatricule(UUID etablissementId, UUID anneeScolaireId);

    Optional<Inscription> findByEleveIdAndEtablissementIdAndAnneeScolaireId(UUID eleveId, UUID etablissementId, UUID anneeScolaireId);

    boolean existsByEtablissementIdAndMatricule(UUID etablissementId, String matricule);

    boolean existsByEtablissementIdAndEleveId(UUID etablissementId, UUID eleveId);
}
