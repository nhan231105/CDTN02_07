package org.example.entity;

public enum UserStatus {
    ACTIVE("Đang hoạt động"),
    LOCKED("Bị khóa");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
