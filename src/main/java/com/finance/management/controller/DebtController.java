package com.finance.management.controller;

import com.finance.management.dto.DebtDto;
import com.finance.management.model.Debt;
import com.finance.management.model.DebtStatus;
import com.finance.management.model.User;
import com.finance.management.repository.UserRepository;
import com.finance.management.service.DebtService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/debts")
public class DebtController {

    @Autowired
    private DebtService debtService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showDebtPage(@RequestParam(required = false) DebtStatus status,
            @RequestParam(defaultValue = "0") Integer month,
            @RequestParam(defaultValue = "0") Integer year,
            @RequestParam(defaultValue = "1") int page,
            Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        int pageSize = 10;
        int displayMonth = (month == 0) ? LocalDate.now().getMonthValue() : month;
        int displayYear = (year == 0) ? LocalDate.now().getYear() : year;

        Page<Debt> debtPage = debtService.findPaginatedByFilter(username, year, month, status, page,
                pageSize);

        model.addAttribute("user", user);
        model.addAttribute("debtPage", debtPage);
        model.addAttribute("debtStatuses", DebtStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);

        List<Integer> years = IntStream.rangeClosed(LocalDate.now().getYear() - 5, LocalDate.now().getYear())
                .boxed().sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList());
        model.addAttribute("years", years);

        if (!model.containsAttribute("debt")) {
            model.addAttribute("debt", new Debt());
        }

        return "debts";
    }

    @PostMapping("/add")
    public String addDebt(@Valid @ModelAttribute("debtDto") DebtDto debtDto, // Menggunakan DebtDto
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.debtDto", result);
            redirectAttributes.addFlashAttribute("debtDto", debtDto);
            return "redirect:/debts";
        }
        debtService.createDebt(debtDto, principal.getName()); // Mengirim DebtDto ke service
        redirectAttributes.addFlashAttribute("successMessage", "Utang berhasil ditambahkan!");
        return "redirect:/debts";
    }

    @GetMapping("/edit/{id}")
    public String showEditDebtForm(@PathVariable Long id, Model model, Principal principal) {
        Debt debt = debtService.getDebtByIdAndUsername(id, principal.getName());
        model.addAttribute("debt", debt);
        model.addAttribute("debtStatuses", DebtStatus.values());
        return "edit-debt";
    }

    @PostMapping("/update/{id}")
    public String updateDebt(@PathVariable Long id,
            @Valid @ModelAttribute("debt") Debt debt,
            BindingResult result,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            debt.setId(id);
            model.addAttribute("debtStatuses", DebtStatus.values());
            return "edit-debt";
        }
        debtService.updateDebt(id, debt, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Utang berhasil diperbarui!");
        return "redirect:/debts";
    }

    @PostMapping("/delete/{id}")
    public String deleteDebt(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            debtService.deleteDebt(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Utang berhasil dihapus!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus: " + e.getMessage());
        }
        return "redirect:/debts";
    }

    @PostMapping("/pay")
    public String processPayment(@RequestParam("debtId") Long debtId,
            @RequestParam("amount") BigDecimal amount,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            debtService.makePayment(debtId, amount, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Pembayaran berhasil dicatat!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal mencatat pembayaran: " + e.getMessage());
        }
        return "redirect:/debts";
    }
}
