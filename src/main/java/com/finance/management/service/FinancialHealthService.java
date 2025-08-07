package com.finance.management.service;

import com.finance.management.dto.HealthMetric;
import com.finance.management.dto.RecommendationDto;
import com.finance.management.model.RiskProfile;
import com.finance.management.model.User;
import com.finance.management.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class FinancialHealthService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private DebtService debtService;

    @Autowired
    private UserRepository userRepository;

    public HealthMetric calculateDebtToAssetRatio(String username) {
        BigDecimal totalAssets = Objects.requireNonNullElse(assetService.getTotalAssetValue(username), BigDecimal.ZERO);
        BigDecimal totalDebts = Objects.requireNonNullElse(debtService.getTotalRemainingDebt(username),
                BigDecimal.ZERO);

        if (totalAssets.compareTo(BigDecimal.ZERO) == 0) {
            return totalDebts.compareTo(BigDecimal.ZERO) == 0 ? new HealthMetric(BigDecimal.ZERO, "Sehat", "bg-success")
                    : new HealthMetric(BigDecimal.ONE, "Berisiko", "bg-danger");
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

    public HealthMetric calculateSavingsRate(String username) {
        BigDecimal totalIncome = Objects.requireNonNullElse(transactionService.getTotalIncome(username),
                BigDecimal.ZERO);
        BigDecimal totalExpense = Objects.requireNonNullElse(transactionService.getTotalExpense(username),
                BigDecimal.ZERO);

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

     public List<RecommendationDto> generateDynamicRecommendations(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<RecommendationDto> recommendations = new ArrayList<>();

        // 1. Dapatkan metrik kesehatan dasar
        HealthMetric savingsRate = calculateSavingsRate(username);
        HealthMetric debtRatio = calculateDebtToAssetRatio(username);
        BigDecimal totalExpense = transactionService.getExpenseForCurrentMonth(username);

        // ATURAN 1: Peringatan Pengeluaran Berlebih (Boros)
        if ("Boros".equals(savingsRate.getStatus())) {
            recommendations.add(new RecommendationDto(
                "Prioritas Utama: Arus Kas Negatif!",
                "Pengeluaran Anda bulan ini lebih besar dari pemasukan. Segera tinjau kembali anggaran dan kurangi pengeluaran tidak esensial.",
                "HIGH"
            ));
        }

        // ATURAN 2: Peringatan Utang Berisiko
        if ("Berisiko".equals(debtRatio.getStatus())) {
            recommendations.add(new RecommendationDto(
                "Peringatan: Tingkat Utang Tinggi!",
                "Rasio utang terhadap aset Anda cukup tinggi. Prioritaskan untuk melunasi utang dengan bunga tertinggi untuk memperbaiki kesehatan keuangan.",
                "HIGH"
            ));
        }
        
        // ATURAN 3: Analisis Kategori Pengeluaran Terbesar
        if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, BigDecimal> expenseSummary = transactionService.getExpenseSummaryForMonth(username, 
                                                                    java.time.LocalDate.now().getYear(), 
                                                                    java.time.LocalDate.now().getMonthValue());
            
            expenseSummary.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> {
                    BigDecimal percentage = entry.getValue().divide(totalExpense, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                    if (percentage.compareTo(new BigDecimal(35)) > 0) {
                        recommendations.add(new RecommendationDto(
                            "Fokus Penghematan",
                            "Pengeluaran terbesar Anda bulan ini (" + percentage.intValue() + "%) ada di kategori '" + entry.getKey() + "'. Coba lihat detailnya, mungkin ada yang bisa dihemat.",
                            "MEDIUM"
                        ));
                    }
                });
        }

        // ATURAN 4: Rekomendasi Investasi (jika kondisi keuangan sehat)
        if ("Sangat Baik".equals(savingsRate.getStatus()) && "Sehat".equals(debtRatio.getStatus())) {
            String investmentAdvice = getInvestmentAdvice(user.getRiskProfile());
            recommendations.add(new RecommendationDto(
                "Peluang Investasi",
                "Kondisi keuangan Anda sangat sehat! Sesuai profil risiko Anda (" + (user.getRiskProfile() != null ? user.getRiskProfile().getDisplayName() : "Belum diatur") + "), ini saat yang tepat untuk mempertimbangkan menambah investasi. " + investmentAdvice,
                "LOW"
            ));
        }

        return recommendations;
    }

    private String getInvestmentAdvice(RiskProfile profile) {
        if (profile == null) {
            return "Atur dulu profil risiko Anda di halaman profil untuk mendapatkan saran yang lebih spesifik.";
        }
        switch (profile) {
            case KONSERVATIF: return "Pertimbangkan instrumen stabil seperti Emas atau Reksa Dana Pasar Uang.";
            case MODERAT: return "Portofolio seimbang seperti Reksa Dana Campuran atau saham Blue Chip bisa menjadi pilihan yang baik.";
            case AGRESIF: return "Anda bisa melirik instrumen dengan potensi pertumbuhan tinggi seperti Reksa Dana Saham.";
            default: return "";
        }
    }
}