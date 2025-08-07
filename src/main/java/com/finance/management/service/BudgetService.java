package com.finance.management.service;

import com.finance.management.model.Budget;
import com.finance.management.model.User;
import com.finance.management.repository.BudgetRepository;
import com.finance.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BudgetService {

  @Autowired
  private BudgetRepository budgetRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TransactionService transactionService;

  /**
   * Membuat atau memperbarui anggaran untuk kategori tertentu.
   */
  @Transactional
  public Budget createOrUpdateBudget(Budget budget, String username) {
    User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    budget.setUser(user);

    // Cek apakah sudah ada budget untuk kategori, bulan, dan tahun yang sama
    budgetRepository.findByCategoryAndUser_IdAndBudgetYearAndBudgetMonth(
        budget.getCategory(), user.getId(), budget.getBudgetYear(), budget.getBudgetMonth())
        .ifPresent(existingBudget -> budget.setId(existingBudget.getId())); // Jika ada, set ID agar menjadi update

    return budgetRepository.save(budget);
  }

  /**
   * Mengambil semua anggaran untuk pengguna pada bulan dan tahun tertentu.
   */
  public List<Budget> getBudgetsForMonth(String username, int year, int month) {
    User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    return budgetRepository.findByUser_IdAndBudgetYearAndBudgetMonth(user.getId(), year, month);
  }

  /**
   * Mengambil data anggaran beserta total pengeluaran aktualnya.
   * Ini akan digunakan di dashboard.
   */
  public List<BudgetTrackingInfo> getBudgetTrackingInfo(String username, int year, int month) {
    List<Budget> budgets = getBudgetsForMonth(username, year, month);
    Map<String, BigDecimal> expenseSummary = transactionService.getExpenseSummaryForMonth(username, year, month);

    return budgets.stream().map(budget -> {
      BigDecimal spentAmount = expenseSummary.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
      return new BudgetTrackingInfo(budget, spentAmount);
    }).collect(Collectors.toList());
  }

  // Inner class/DTO untuk membawa data tracking ke view
  public static class BudgetTrackingInfo {
    private Budget budget;
    private BigDecimal spentAmount;

    public BudgetTrackingInfo(Budget budget, BigDecimal spentAmount) {
      this.budget = budget;
      this.spentAmount = spentAmount;
    }

    public Budget getBudget() {
      return budget;
    }

    public BigDecimal getSpentAmount() {
      return spentAmount;
    }

    public int getPercentageSpent() {
      if (budget.getAmount().compareTo(BigDecimal.ZERO) == 0)
        return 0;
      return spentAmount.multiply(new BigDecimal(100)).divide(budget.getAmount(), 0, BigDecimal.ROUND_HALF_UP)
          .intValue();
    }

    public String getProgressBarColor() {
      int percentage = getPercentageSpent();
      if (percentage > 90)
        return "bg-danger";
      if (percentage > 75)
        return "bg-warning";
      return "bg-success";
    }
  }
}
