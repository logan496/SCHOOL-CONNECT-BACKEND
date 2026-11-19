package com.schoolconnect.api.identity.service;

import com.schoolconnect.api.identity.dto.RoleDtos.AttribuerRoleRequest;
import com.schoolconnect.api.identity.dto.RoleDtos.RoleUtilisateurView;
import com.schoolconnect.api.identity.dto.RoleDtos.UtilisateurRolesView;
import com.schoolconnect.api.identity.model.RoleType;
import com.schoolconnect.api.identity.model.RoleUtilisateur;
import com.schoolconnect.api.identity.model.Utilisateur;
import com.schoolconnect.api.identity.repository.RoleUtilisateurRepository;
import com.schoolconnect.api.identity.repository.UtilisateurRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleService {

    private final RoleUtilisateurRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;

    public RoleService(RoleUtilisateurRepository roleRepository,
                       UtilisateurRepository utilisateurRepository) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** Attribue un rôle à un utilisateur dans un établissement (ou global pour SUPER_ADMIN). */
    @Transactional
    public RoleUtilisateurView attribuerRole(AttribuerRoleRequest request) {
        // Vérifier que l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findById(request.utilisateurId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        // SUPER_ADMIN ne nécessite pas d'établissement, les autres oui
        if (request.role() != RoleType.SUPER_ADMIN && request.etablissementId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un établissement est obligatoire pour le rôle " + request.role());
        }
        if (request.role() == RoleType.SUPER_ADMIN && request.etablissementId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le rôle SUPER_ADMIN ne doit pas être lié à un établissement");
        }

        // Vérifier qu'il n'a pas déjà ce rôle actif
        if (roleRepository.existsByUtilisateurIdAndEtablissementIdAndRoleAndActifTrue(
                request.utilisateurId(), request.etablissementId(), request.role())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "L'utilisateur possède déjà ce rôle actif");
        }

        RoleUtilisateur role = new RoleUtilisateur(
                request.utilisateurId(), request.etablissementId(), request.role());
        role.setDateDebut(request.dateDebut() != null ? request.dateDebut() : LocalDate.now());
        role.setDateFin(request.dateFin());

        role = roleRepository.save(role);
        return toView(role);
    }

    /** Révoque (désactive) un rôle existant. */
    @Transactional
    public RoleUtilisateurView revoquerRole(UUID roleId) {
        RoleUtilisateur role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rôle introuvable"));

        if (!role.isActif()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce rôle est déjà révoqué");
        }

        role.setActif(false);
        role.setDateFin(LocalDate.now());
        return toView(role);
    }

    /** Liste les rôles actifs d'un utilisateur. */
    @Transactional(readOnly = true)
    public UtilisateurRolesView rolesUtilisateur(UUID utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        List<RoleUtilisateurView> roles = roleRepository.findByUtilisateurIdAndActifTrue(utilisateurId)
                .stream()
                .map(RoleService::toView)
                .toList();

        return new UtilisateurRolesView(
                utilisateur.getId(), utilisateur.getNom(), utilisateur.getPrenom(), roles);
    }

    private static RoleUtilisateurView toView(RoleUtilisateur role) {
        return new RoleUtilisateurView(
                role.getId(),
                role.getUtilisateurId(),
                role.getEtablissementId(),
                role.getRole(),
                role.isActif(),
                role.getDateDebut(),
                role.getDateFin());
    }
}
