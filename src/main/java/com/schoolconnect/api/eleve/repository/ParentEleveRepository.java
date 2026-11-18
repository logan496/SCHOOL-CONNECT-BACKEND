package com.schoolconnect.api.eleve.repository;

import com.schoolconnect.api.eleve.model.ParentEleve;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentEleveRepository extends JpaRepository<ParentEleve, UUID> {

    List<ParentEleve> findByEleveId(UUID eleveId);

    Optional<ParentEleve> findByIdAndEleveId(UUID id, UUID eleveId);

    Optional<ParentEleve> findByUtilisateurIdAndEleveId(UUID utilisateurId, UUID eleveId);
}
