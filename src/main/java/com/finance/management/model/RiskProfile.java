package com.finance.management.model;

public enum RiskProfile {
    KONSERVATIF("Konservatif"),
    MODERAT("Moderat"),
    AGRESIF("Agresif");

    private final String displayName;

    RiskProfile(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
