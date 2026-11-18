package com.schoolconnect.api.school.repository;

import com.schoolconnect.api.school.model.Classe;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClasseRepository extends JpaRepository<Classe, UUID> {

    List<Classe> findByEtablissementIdAndAnneeScolaireIdOrderByNom(UUID etablissementId, UUID anneeScolaireId);

    boolean existsByIdAndEtablissementId(UUID id, UUID etablissementId);

    boolean existsByAnneeScolaireIdAndNom(UUID anneeScolaireId, String nom);
}
