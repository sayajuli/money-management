package com.finance.management.service;

import com.finance.management.dto.HealthMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
public class FinancialHealthService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private DebtService debtService;

    /**
     * Menghitung Rasio Utang terhadap Aset dan memberikan statusnya.
     * Versi ini sudah aman dari NullPointerException.
     */
    public HealthMetric calculateDebtToAssetRatio(String username) {
        // Ambil data, dan langsung beri nilai default 0 jika null
        BigDecimal totalAssets = Objects.requireNonNullElse(assetService.getTotalAssetValue(username), BigDecimal.ZERO);
        BigDecimal totalDebts = Objects.requireNonNullElse(debtService.getTotalRemainingDebt(username), BigDecimal.ZERO);

        // Menghindari pembagian dengan nol
        if (totalAssets.compareTo(BigDecimal.ZERO) == 0) {
            return totalDebts.compareTo(BigDecimal.ZERO) == 0 ?
                   new HealthMetric(BigDecimal.ZERO, "Sehat", "bg-success") :
                   new HealthMetric(BigDecimal.ONE, "Berisiko", "bg-danger");
        }

        BigDecimal ratio = totalDebts.divide(totalAssets, 4, RoundingMode.HALF_UP);

        if (ratio.compareTo(new BigDecimal("0.4")) < 0) {
            return new HealthMetric(ratio, "Sehat", "bg-success");
        } else if (ratio.compareTo(new BigDecimal("0.6")) <= 0) {
            return new HealthMetric(ratio, "Perlu Perhatian", "bg-warning");
        } else {
            return new HealthMetric(ratio, "Berisiko", "bg-danger");
        }
    }

    /**
     * Menghitung Tingkat Tabungan (Savings Rate) dan memberikan statusnya.
     * Versi ini sudah aman dari NullPointerException.
     */
    public HealthMetric calculateSavingsRate(String username) {
        // Ambil data, dan langsung beri nilai default 0 jika null
        BigDecimal totalIncome = Objects.requireNonNullElse(transactionService.getTotalIncome(username), BigDecimal.ZERO);
        BigDecimal totalExpense = Objects.requireNonNullElse(transactionService.getTotalExpense(username), BigDecimal.ZERO);

        // Menghindari pembagian dengan nol
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return new HealthMetric(BigDecimal.ZERO, "N/A", "bg-secondary");
        }

        BigDecimal savings = totalIncome.subtract(totalExpense);
        BigDecimal ratio = savings.divide(totalIncome, 4, RoundingMode.HALF_UP);

        if (ratio.compareTo(new BigDecimal("0.2")) > 0) {
            return new HealthMetric(ratio, "Sangat Baik", "bg-success");
        } else if (ratio.compareTo(new BigDecimal("0.1")) >= 0) {
            return new HealthMetric(ratio, "Cukup", "bg-warning");
        } else {
            if (ratio.compareTo(BigDecimal.ZERO) < 0) {
                return new HealthMetric(ratio, "Boros", "bg-danger");
            }
            return new HealthMetric(ratio, "Perlu Ditingkatkan", "bg-danger");
        }
    }
}