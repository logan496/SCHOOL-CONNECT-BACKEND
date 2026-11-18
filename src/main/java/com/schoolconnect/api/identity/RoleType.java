package com.schoolconnect.api.identity;

/**
 * Rôles du personnel et de la plateforme. Le profil "parent" n'est pas un rôle :
 * il découle des liens parent_eleve.
 */
public enum RoleType {
    SUPER_ADMIN, DIRECTION, SECRETARIAT, SURVEILLANT, ENSEIGNANT, ECONOMAT
}
