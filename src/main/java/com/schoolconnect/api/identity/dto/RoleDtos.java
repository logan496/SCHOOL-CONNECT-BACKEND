package com.schoolconnect.api.identity.dto;

import com.schoolconnect.api.identity.model.RoleType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class RoleDtos {

    private RoleDtos() {
    }

    /** Requête pour attribuer un rôle à un utilisateur. */
    public record AttribuerRoleRequest(
            @NotNull UUID utilisateurId,
            UUID etablissementId,
            @NotNull RoleType role,
            LocalDate dateDebut,
            LocalDate dateFin) {
    }

    /** Requête pour révoquer (désactiver) un rôle. */
    public record RevoquerRoleRequest(
            @NotNull UUID roleId) {
    }

    /** Vue d'un rôle attribué. */
    public record RoleUtilisateurView(
            UUID id,
            UUID utilisateurId,
            UUID etablissementId,
            RoleType role,
            boolean actif,
            LocalDate dateDebut,
            LocalDate dateFin) {
    }

    /** Vue résumée d'un utilisateur avec ses rôles. */
    public record UtilisateurRolesView(
            UUID utilisateurId,
            String nom,
            String prenom,
            List<RoleUtilisateurView> roles) {
    }
}
