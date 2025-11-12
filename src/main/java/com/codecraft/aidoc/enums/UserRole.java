package com.codecraft.aidoc.enums;

/**
 * Supported user roles within the AIDocGen platform.
 */
public enum UserRole {
    STANDARD,
    PREMIUM,
    ADMIN;

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isPremium() {
        return this == PREMIUM || this == ADMIN;
    }
}
