package com.schoolconnect.api.school;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClasseRepository extends JpaRepository<Classe, UUID> {

    List<Classe> findByEtablissementIdAndAnneeScolaireIdOrderByNom(UUID etablissementId, UUID anneeScolaireId);

    boolean existsByIdAndEtablissementId(UUID id, UUID etablissementId);

    boolean existsByAnneeScolaireIdAndNom(UUID anneeScolaireId, String nom);
}
