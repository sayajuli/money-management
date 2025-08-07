package com.finance.management.dto;

import com.finance.management.model.RiskProfile;

import jakarta.validation.constraints.NotEmpty;

public class ProfileUpdateDto {
    @NotEmpty(message = "Nama tidak boleh kosong")
    private String name;
    
    @NotEmpty(message = "Email tidak boleh kosong")
    private String email;

    private RiskProfile riskProfile;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public RiskProfile getRiskProfile() { return riskProfile; }
    public void setRiskProfile(RiskProfile riskProfile) { this.riskProfile = riskProfile; }
}
