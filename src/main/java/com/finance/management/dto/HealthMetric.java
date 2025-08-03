package com.finance.management.dto;

import java.math.BigDecimal;

public class HealthMetric {
    private BigDecimal value;
    private String status;
    private String colorClass;

    public HealthMetric(BigDecimal value, String status, String colorClass) {
        this.value = value;
        this.status = status;
        this.colorClass = colorClass;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getStatus() {
        return status;
    }

    public String getColorClass() {
        return colorClass;
    }

    public int valueAsPercentage() {
        if (this.value == null) {
            return 0;
        }
        return this.value.multiply(new BigDecimal("100")).intValue();
    }
}