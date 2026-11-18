package com.schoolconnect.api.school.repository;

import com.schoolconnect.api.school.model.AffectationEnseignant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffectationEnseignantRepository extends JpaRepository<AffectationEnseignant, UUID> {

    List<AffectationEnseignant> findByEnseignantIdAndAnneeScolaireId(UUID enseignantId, UUID anneeScolaireId);

    boolean existsByAnneeScolaireIdAndClasseIdAndMatiereIdAndEnseignantId(
            UUID anneeScolaireId, UUID classeId, UUID matiereId, UUID enseignantId);
}
