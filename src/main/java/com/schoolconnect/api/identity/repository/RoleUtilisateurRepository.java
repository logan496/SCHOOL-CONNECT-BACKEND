package com.schoolconnect.api.identity.repository;

import com.schoolconnect.api.identity.model.RoleType;
import com.schoolconnect.api.identity.model.RoleUtilisateur;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleUtilisateurRepository extends JpaRepository<RoleUtilisateur, UUID> {

    List<RoleUtilisateur> findByUtilisateurIdAndActifTrue(UUID utilisateurId);

    boolean existsByRole(RoleType role);

    boolean existsByUtilisateurIdAndEtablissementIdAndRoleAndActifTrue(UUID utilisateurId, UUID etablissementId, RoleType role);

    List<RoleUtilisateur> findByEtablissementIdAndActifTrue(UUID etablissementId);

    List<RoleUtilisateur> findByRoleAndActifTrue(RoleType role);

    List<RoleUtilisateur> findByEtablissementIdAndRoleAndActifTrue(UUID etablissementId, RoleType role);
}
