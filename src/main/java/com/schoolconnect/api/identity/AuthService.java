package com.schoolconnect.api.identity;

import com.schoolconnect.api.identity.AuthDtos.AuthResponse;
import com.schoolconnect.api.identity.AuthDtos.LoginRequest;
import com.schoolconnect.api.identity.AuthDtos.RegisterRequest;
import com.schoolconnect.api.identity.AuthDtos.RoleView;
import com.schoolconnect.api.identity.AuthDtos.UserView;
import com.schoolconnect.api.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurs;
    private final RoleUtilisateurRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurs,
                       RoleUtilisateurRepository roles,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.utilisateurs = utilisateurs;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Inscription publique : crée un compte parent/tuteur (aucun rôle de personnel). */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String telephone = PhoneNumbers.normalize(request.telephone());
        String email = normalizeEmail(request.email());

        if (telephone == null && email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un numéro de téléphone ou un email est obligatoire");
        }
        if (telephone != null && !PhoneNumbers.isValid(telephone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Numéro de téléphone invalide (8 à 15 chiffres, préfixe + autorisé)");
        }
        if (telephone != null && utilisateurs.existsByTelephone(telephone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro de téléphone est déjà utilisé");
        }
        if (email != null && utilisateurs.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }

        Utilisateur user = new Utilisateur();
        user.setNom(request.nom().trim());
        user.setPrenom(request.prenom().trim());
        user.setTelephone(telephone);
        user.setEmail(email);
        user.setMotDePasseHash(passwordEncoder.encode(request.motDePasse()));
        if (request.langue() != null) {
            user.setLangue(request.langue());
        }
        user = utilisateurs.save(user);
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifiant = request.identifiant().trim();
        Utilisateur user = (identifiant.contains("@")
                ? utilisateurs.findByEmail(identifiant.toLowerCase(Locale.ROOT))
                : utilisateurs.findByTelephone(PhoneNumbers.normalize(identifiant)))
                .orElseThrow(AuthService::invalidCredentials);

        if (user.getMotDePasseHash() == null || !passwordEncoder.matches(request.motDePasse(), user.getMotDePasseHash())) {
            throw invalidCredentials();
        }
        if (user.getStatut() != StatutUtilisateur.ACTIF) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte non actif");
        }

        user.setDerniereConnexion(Instant.now());
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public UserView currentUser(String userId) {
        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton invalide");
        }
        Utilisateur user = utilisateurs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
        return toView(user, roles.findByUtilisateurIdAndActifTrue(user.getId()));
    }

    private AuthResponse buildResponse(Utilisateur user) {
        List<RoleUtilisateur> userRoles = roles.findByUtilisateurIdAndActifTrue(user.getId());
        String token = jwtService.generate(user.getId(), authorities(userRoles));
        return new AuthResponse(token, "Bearer", jwtService.expiresInSeconds(), toView(user, userRoles));
    }

    /**
     * Autorités placées dans le jeton : "ROLE", "ETAB:<id>" et "ROLE:<id>" (rôle dans un établissement donné).
     */
    private static List<String> authorities(List<RoleUtilisateur> userRoles) {
        List<String> authorities = new ArrayList<>();
        for (RoleUtilisateur r : userRoles) {
            authorities.add(r.getRole().name());
            if (r.getEtablissementId() != null) {
                authorities.add("ETAB:" + r.getEtablissementId());
                authorities.add(r.getRole().name() + ":" + r.getEtablissementId());
            }
        }
        return authorities.stream().distinct().toList();
    }

    private static UserView toView(Utilisateur user, List<RoleUtilisateur> userRoles) {
        List<RoleView> roleViews = userRoles.stream()
                .map(r -> new RoleView(r.getRole(), r.getEtablissementId()))
                .toList();
        return new UserView(user.getId(), user.getNom(), user.getPrenom(), user.getTelephone(),
                user.getEmail(), user.getLangue(), roleViews);
    }

    private static String normalizeEmail(String email) {
        return (email == null || email.isBlank()) ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
    }
}
