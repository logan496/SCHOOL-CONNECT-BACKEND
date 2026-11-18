package com.schoolconnect.api.eleve.repository;

import com.schoolconnect.api.eleve.model.Inscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscriptionRepository extends JpaRepository<Inscription, UUID> {

    List<Inscription> findByEtablissementIdAndAnneeScolaireIdOrderByMatricule(UUID etablissementId, UUID anneeScolaireId);

    Optional<Inscription> findByEleveIdAndEtablissementIdAndAnneeScolaireId(UUID eleveId, UUID etablissementId, UUID anneeScolaireId);

    boolean existsByEtablissementIdAndMatricule(UUID etablissementId, String matricule);

    boolean existsByEtablissementIdAndEleveId(UUID etablissementId, UUID eleveId);
}
