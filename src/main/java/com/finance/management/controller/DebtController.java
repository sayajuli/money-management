package com.finance.management.controller;

import com.finance.management.dto.DebtDto;
import com.finance.management.model.Debt;
import com.finance.management.model.DebtStatus;
import com.finance.management.service.DebtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/debts")
public class DebtController {

    @Autowired
    private DebtService debtService;

    @GetMapping
    public String showDebtPage(@RequestParam(required = false) DebtStatus status, Model model, Principal principal) {
        String username = principal.getName();
        List<Debt> debts;

        if (status != null) {
            debts = debtService.getDebtsByStatus(username, status);
        } else {
            debts = debtService.getDebtsForUser(username);
        }

        model.addAttribute("debts", debts);
        model.addAttribute("debtStatuses", DebtStatus.values());
        model.addAttribute("selectedStatus", status);

        // Selalu siapkan objek baru untuk form tambah
        model.addAttribute("debt", new Debt());

        return "debts";
    }

    @PostMapping("/add")
    public String addDebt(@Valid @ModelAttribute("debt") DebtDto debt,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // Jika validasi gagal, kita perlu menyiapkan model lagi untuk halaman debts
            // Cara termudah adalah redirect kembali dengan error
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.debt", result);
            redirectAttributes.addFlashAttribute("debt", debt);
            return "redirect:/debts";
        }
        debtService.createDebt(debt, principal.getName());
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
            Model model, // Tambahkan model untuk mengirim data kembali jika error
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            debt.setId(id); // Pastikan ID tidak hilang
            model.addAttribute("debtStatuses", DebtStatus.values()); // Kirim lagi status untuk dropdown
            return "edit-debt"; // Kembali ke halaman edit dengan pesan error
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
