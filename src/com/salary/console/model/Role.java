package com.salary.console.model;

public enum Role {
    ADMIN,
    EMPLOYEE;

    public String labelKey() {
        return "role." + name().toLowerCase();
    }
}
