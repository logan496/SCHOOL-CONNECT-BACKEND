package com.schoolconnect.api.identity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleUtilisateurRepository extends JpaRepository<RoleUtilisateur, UUID> {

    List<RoleUtilisateur> findByUtilisateurIdAndActifTrue(UUID utilisateurId);

    boolean existsByRole(RoleType role);
}
