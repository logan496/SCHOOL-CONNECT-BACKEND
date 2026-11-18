package com.schoolconnect.api.identity;

import java.util.regex.Pattern;

public final class PhoneNumbers {

    private static final Pattern VALID = Pattern.compile("^\\+?[0-9]{8,15}$");

    private PhoneNumbers() {
    }

    /** Retire espaces, tirets, points et parenthèses. Retourne null si la valeur est vide. */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.replaceAll("[\\s\\-.()]", "");
    }

    public static boolean isValid(String normalized) {
        return normalized != null && VALID.matcher(normalized).matches();
    }
}
