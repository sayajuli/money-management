package com.finance.management.controller;

import com.finance.management.dto.HealthMetric;
import com.finance.management.model.User;
import com.finance.management.repository.UserRepository;
import com.finance.management.service.AssetService;
import com.finance.management.service.BudgetService; 
import com.finance.management.service.DebtService;
import com.finance.management.service.FinancialHealthService;
import com.finance.management.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Controller
public class DashboardController {

    @Autowired
    private AssetService assetService;
    @Autowired
    private DebtService debtService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private FinancialHealthService financialHealthService;
    @Autowired
    private BudgetService budgetService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping({ "/", "/dashboard" })
    public String showDashboard(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        BigDecimal totalAset = Objects.requireNonNullElse(assetService.getTotalAssetValue(username), BigDecimal.ZERO);
        BigDecimal totalUtang = Objects.requireNonNullElse(debtService.getTotalRemainingDebt(username),
                BigDecimal.ZERO);
        BigDecimal totalPemasukanBulanIni = Objects
                .requireNonNullElse(transactionService.getIncomeForCurrentMonth(username), BigDecimal.ZERO);
        BigDecimal totalPengeluaranBulanIni = Objects
                .requireNonNullElse(transactionService.getExpenseForCurrentMonth(username), BigDecimal.ZERO);
        BigDecimal kekayaanBersih = totalAset.subtract(totalUtang);
        BigDecimal totalCicilanBulanIni = Objects.requireNonNullElse(debtService.getTotalMonthlyInstallment(username), BigDecimal.ZERO);

        HealthMetric debtToAssetMetric = financialHealthService.calculateDebtToAssetRatio(username);
        HealthMetric savingsRateMetric = financialHealthService.calculateSavingsRate(username);

        List<BudgetService.BudgetTrackingInfo> budgetInfos = budgetService.getBudgetTrackingInfo(username, currentYear, currentMonth);

        model.addAttribute("user", user);
        model.addAttribute("totalAset", totalAset);
        model.addAttribute("totalUtang", totalUtang);
        model.addAttribute("totalPemasukanBulanIni", totalPemasukanBulanIni);
        model.addAttribute("totalPengeluaranBulanIni", totalPengeluaranBulanIni);
        model.addAttribute("kekayaanBersih", kekayaanBersih);
        model.addAttribute("debtToAssetMetric", debtToAssetMetric);
        model.addAttribute("savingsRateMetric", savingsRateMetric);
        model.addAttribute("budgetInfos", budgetInfos);
        model.addAttribute("totalCicilanBulanIni", totalCicilanBulanIni);

        return "dashboard";
    }
}