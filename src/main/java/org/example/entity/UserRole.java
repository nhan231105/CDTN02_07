package org.example.entity;

public enum UserRole {
    STUDENT("Sinh viên"),
    EMPLOYER("Nhà tuyển dụng"),
    ADMIN("Quản trị viên");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
