package com.finance.management.controller;

import com.finance.management.model.Budget;
import com.finance.management.model.User;
import com.finance.management.repository.UserRepository;
import com.finance.management.service.BudgetService;
import com.finance.management.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showBudgetPage(@RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) Integer year,
                                 Model model,
                                 Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        String username = principal.getName();
        
        int currentMonth = (month == null) ? LocalDate.now().getMonthValue() : month;
        int currentYear = (year == null) ? LocalDate.now().getYear() : year;

        model.addAttribute("user", user);

        // Mengambil data anggaran yang sudah ada untuk bulan & tahun yang dipilih
        List<BudgetService.BudgetTrackingInfo> budgetInfos = budgetService.getBudgetTrackingInfo(username, currentYear, currentMonth);
        model.addAttribute("budgetInfos", budgetInfos);

        // Menyiapkan data untuk dropdown filter
        model.addAttribute("selectedMonth", currentMonth);
        model.addAttribute("selectedYear", currentYear);
        
        // Membuat daftar tahun untuk dropdown
        List<Integer> years = IntStream.rangeClosed(LocalDate.now().getYear() - 5, LocalDate.now().getYear() + 1)
                                       .boxed().sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList());
        model.addAttribute("years", years);
        
        // Menyiapkan daftar kategori pengeluaran yang sudah ada untuk dropdown form
        model.addAttribute("expenseCategories", transactionService.getUniqueCategories(username));

        // Menyiapkan objek kosong untuk form tambah anggaran baru
        if (!model.containsAttribute("budget")) {
            Budget newBudget = new Budget();
            newBudget.setBudgetMonth(currentMonth);
            newBudget.setBudgetYear(currentYear);
            model.addAttribute("budget", newBudget);
        }

        return "budgets"; // Mengembalikan file budgets.html
    }

    @PostMapping("/add")
    public String addOrUpdateBudget(@Valid @ModelAttribute("budget") Budget budget,
                                    BindingResult result,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        
        // Menyimpan parameter bulan dan tahun untuk redirect
        String redirectUrl = "redirect:/budgets?year=" + budget.getBudgetYear() + "&month=" + budget.getBudgetMonth();
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.budget", result);
            redirectAttributes.addFlashAttribute("budget", budget);
            return redirectUrl;
        }

        budgetService.createOrUpdateBudget(budget, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Anggaran untuk kategori '" + budget.getCategory() + "' berhasil disimpan!");
        
        return redirectUrl;
    }
}
