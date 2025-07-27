package com.finance.management.controller;

import com.finance.management.model.Transaction;
import com.finance.management.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public String showTransactionPage(@RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            Model model,
            Principal principal) {
        String username = principal.getName();
        int pageSize = 10; // Jumlah data per halaman

        Page<Transaction> transactionPage;

        if (category != null && !category.isEmpty()) {
            transactionPage = transactionService.findPaginatedByCategory(username, category, page, pageSize);
            model.addAttribute("selectedCategory", category);
        } else {
            transactionPage = transactionService.findPaginated(username, page, pageSize);
        }

        model.addAttribute("transactionPage", transactionPage);
        model.addAttribute("categories", transactionService.getUniqueCategories(username));

        if (!model.containsAttribute("transaction")) {
            model.addAttribute("transaction", new Transaction());
        }

        return "transactions";
    }

    @PostMapping("/add")
    public String addTransaction(@Valid @ModelAttribute("transaction") Transaction transaction,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.transaction", result);
            redirectAttributes.addFlashAttribute("transaction", transaction);
            return "redirect:/transactions";
        }

        transactionService.createTransaction(transaction, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Transaksi berhasil ditambahkan!");

        return "redirect:/transactions";
    }

    @GetMapping("/edit/{id}")
    public String showEditTransactionForm(@PathVariable Long id, Model model, Principal principal) {
        Transaction transaction = transactionService.getTransactionByIdAndUsername(id, principal.getName());
        model.addAttribute("transaction", transaction);
        return "edit-transaction";
    }

    @PostMapping("/update/{id}")
    public String updateTransaction(@PathVariable Long id,
            @Valid @ModelAttribute("transaction") Transaction transaction,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            transaction.setId(id);
            return "edit-transaction";
        }
        transactionService.updateTransaction(id, transaction, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Transaksi berhasil diperbarui!");
        return "redirect:/transactions";
    }

    @PostMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            transactionService.deleteTransaction(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Transaksi berhasil dihapus!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus transaksi: " + e.getMessage());
        }
        return "redirect:/transactions";
    }
}
