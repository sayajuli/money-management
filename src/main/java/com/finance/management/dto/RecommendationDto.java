package com.finance.management.dto;

public class RecommendationDto {
    private String title;
    private String message;
    private String priority; // HIGH, MEDIUM, LOW

    public RecommendationDto(String title, String message, String priority) {
        this.title = title;
        this.message = message;
        this.priority = priority;
    }

    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getPriority() { return priority; }

    // Helper untuk warna di Bootstrap
    public String getColorClass() {
        switch (priority) {
            case "HIGH": return "alert-danger";
            case "MEDIUM": return "alert-warning";
            default: return "alert-info";
        }
    }
}
