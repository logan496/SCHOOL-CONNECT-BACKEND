package com.schoolconnect.api.identity.dto;

import com.schoolconnect.api.identity.model.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String nom,
            @NotBlank @Size(max = 100) String prenom,
            @Size(max = 20) String telephone,
            @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72) String motDePasse,
            @Pattern(regexp = "fr|en") String langue) {
    }

    public record LoginRequest(
            @NotBlank String identifiant,
            @NotBlank String motDePasse) {
    }

    public record AuthResponse(String token, String tokenType, long expiresIn, UserView user) {
    }

    public record RoleView(RoleType role, UUID etablissementId) {
    }

    public record UserView(
            UUID id, String nom, String prenom, String telephone, String email, String langue,
            List<RoleView> roles) {
    }
}
