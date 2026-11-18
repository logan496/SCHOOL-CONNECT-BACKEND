package com.schoolconnect.api.school;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnneeScolaireRepository extends JpaRepository<AnneeScolaire, UUID> {

    List<AnneeScolaire> findByEtablissementIdOrderByDateDebutDesc(UUID etablissementId);

    Optional<AnneeScolaire> findByEtablissementIdAndCouranteTrue(UUID etablissementId);

    boolean existsByIdAndEtablissementId(UUID id, UUID etablissementId);

    boolean existsByEtablissementIdAndLibelle(UUID etablissementId, String libelle);
}
