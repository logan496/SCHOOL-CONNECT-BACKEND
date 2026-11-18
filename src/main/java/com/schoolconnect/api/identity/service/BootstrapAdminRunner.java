package com.schoolconnect.api.identity.service;

import com.schoolconnect.api.identity.model.RoleType;
import com.schoolconnect.api.identity.model.RoleUtilisateur;
import com.schoolconnect.api.identity.model.Utilisateur;
import com.schoolconnect.api.identity.repository.RoleUtilisateurRepository;
import com.schoolconnect.api.identity.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crée le premier super-admin au démarrage si SCHOOLCONNECT_ADMIN_PHONE et SCHOOLCONNECT_ADMIN_PASSWORD
 * sont définis et qu'aucun super-admin n'existe encore.
 */
@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final UtilisateurRepository utilisateurs;
    private final RoleUtilisateurRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final String adminPhone;
    private final String adminPassword;

    public BootstrapAdminRunner(UtilisateurRepository utilisateurs,
                                RoleUtilisateurRepository roles,
                                PasswordEncoder passwordEncoder,
                                @Value("${schoolconnect.bootstrap.admin-phone:}") String adminPhone,
                                @Value("${schoolconnect.bootstrap.admin-password:}") String adminPassword) {
        this.utilisateurs = utilisateurs;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.adminPhone = adminPhone;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminPhone.isBlank() || adminPassword.isBlank()) {
            log.info("Aucun super-admin initial configuré (SCHOOLCONNECT_ADMIN_PHONE / SCHOOLCONNECT_ADMIN_PASSWORD).");
            return;
        }
        if (roles.existsByRole(RoleType.SUPER_ADMIN)) {
            return;
        }
        String phone = PhoneNumbers.normalize(adminPhone);
        if (!PhoneNumbers.isValid(phone) || adminPassword.length() < 8) {
            log.warn("Super-admin initial ignoré : téléphone invalide ou mot de passe de moins de 8 caractères.");
            return;
        }

        Utilisateur admin = utilisateurs.findByTelephone(phone).orElseGet(() -> {
            Utilisateur u = new Utilisateur();
            u.setNom("Super");
            u.setPrenom("Admin");
            u.setTelephone(phone);
            u.setMotDePasseHash(passwordEncoder.encode(adminPassword));
            return utilisateurs.save(u);
        });
        roles.save(new RoleUtilisateur(admin.getId(), null, RoleType.SUPER_ADMIN));
        log.info("Super-admin initial créé pour le numéro {}.", phone);
    }
}
