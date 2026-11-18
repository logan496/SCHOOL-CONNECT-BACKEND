package com.schoolconnect.api.school.repository;

import com.schoolconnect.api.school.model.AnneeScolaire;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnneeScolaireRepository extends JpaRepository<AnneeScolaire, UUID> {

    List<AnneeScolaire> findByEtablissementIdOrderByDateDebutDesc(UUID etablissementId);

    Optional<AnneeScolaire> findByEtablissementIdAndCouranteTrue(UUID etablissementId);

    boolean existsByIdAndEtablissementId(UUID id, UUID etablissementId);

    boolean existsByEtablissementIdAndLibelle(UUID etablissementId, String libelle);
}
