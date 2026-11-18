package com.schoolconnect.api.eleve;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParentEleveRepository extends JpaRepository<ParentEleve, UUID> {

    List<ParentEleve> findByEleveId(UUID eleveId);

    Optional<ParentEleve> findByIdAndEleveId(UUID id, UUID eleveId);

    Optional<ParentEleve> findByUtilisateurIdAndEleveId(UUID utilisateurId, UUID eleveId);
}
