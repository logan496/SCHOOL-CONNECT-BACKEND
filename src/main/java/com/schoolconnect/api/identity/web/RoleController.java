package com.schoolconnect.api.identity.web;

import com.schoolconnect.api.identity.dto.RoleDtos.AttribuerRoleRequest;
import com.schoolconnect.api.identity.dto.RoleDtos.RoleUtilisateurView;
import com.schoolconnect.api.identity.dto.RoleDtos.UtilisateurRolesView;
import com.schoolconnect.api.identity.service.RoleService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /** POST /api/v1/roles — Attribue un rôle à un utilisateur. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION')")
    public RoleUtilisateurView attribuer(@Valid @RequestBody AttribuerRoleRequest request) {
        return roleService.attribuerRole(request);
    }

    /** DELETE /api/v1/roles/{roleId} — Révoque un rôle. */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION')")
    public RoleUtilisateurView revoquer(@PathVariable UUID roleId) {
        return roleService.revoquerRole(roleId);
    }

    /** GET /api/v1/roles/utilisateur/{utilisateurId} — Liste les rôles actifs d'un utilisateur. */
    @GetMapping("/utilisateur/{utilisateurId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('DIRECTION') or hasAuthority('SECRETARIAT')")
    public UtilisateurRolesView rolesUtilisateur(@PathVariable UUID utilisateurId) {
        return roleService.rolesUtilisateur(utilisateurId);
    }
}
