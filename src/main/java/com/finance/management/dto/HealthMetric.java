package com.finance.management.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) untuk membawa hasil metrik kesehatan keuangan.
 * Ini membuat pengiriman data ke template menjadi lebih rapi.
 */
public class HealthMetric {
    private BigDecimal value; // Nilai rasio (misal: 0.35)
    private String status;    // Status teks (misal: "Sehat")
    private String colorClass; // Kelas warna Bootstrap (misal: "bg-success")

    public HealthMetric(BigDecimal value, String status, String colorClass) {
        this.value = value;
        this.status = status;
        this.colorClass = colorClass;
    }

    // Getters
    public BigDecimal getValue() {
        return value;
    }

    public String getStatus() {
        return status;
    }

    public String getColorClass() {
        return colorClass;
    }

    /**
     * Method helper untuk mendapatkan nilai dalam bentuk persentase (misal: 35).
     * Ini yang dipanggil oleh Thymeleaf di HTML.
     * @return Nilai rasio dalam format integer persentase.
     */
    public int valueAsPercentage() {
        if (this.value == null) {
            return 0;
        }
        // Mengalikan nilai rasio (misal: 0.35) dengan 100 untuk mendapatkan 35
        return this.value.multiply(new BigDecimal("100")).intValue();
    }
}