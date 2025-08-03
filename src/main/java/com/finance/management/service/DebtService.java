package com.finance.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.management.dto.DebtDto;
import com.finance.management.model.Debt;
import com.finance.management.model.DebtStatus;
import com.finance.management.model.Transaction;
import com.finance.management.model.TransactionType;
import com.finance.management.model.User;
import com.finance.management.repository.DebtRepository;
import com.finance.management.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DebtService {
    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @Transactional
    public void makePayment(Long debtId, BigDecimal paymentAmount, String username) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Jumlah pembayaran harus lebih dari nol.");
        }

        Debt debt = getDebtByIdAndUsername(debtId, username);

        BigDecimal newRemainingAmount = debt.getRemainingAmount().subtract(paymentAmount);
        debt.setRemainingAmount(newRemainingAmount);

        if (newRemainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            debt.setRemainingAmount(BigDecimal.ZERO);
            debt.setStatus(DebtStatus.PAID);
        }

        debtRepository.save(debt);

        Transaction paymentTransaction = new Transaction();
        paymentTransaction.setAmount(paymentAmount);
        paymentTransaction.setCategory("Pembayaran Utang");
        paymentTransaction.setDescription("Pembayaran untuk utang kepada: " + debt.getLenderName());
        paymentTransaction.setType(TransactionType.EXPENSE);
        paymentTransaction.setTransactionDate(LocalDate.now());

        transactionService.createTransaction(paymentTransaction, username);
    }

    @Transactional
    public Debt createDebt(DebtDto debtDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        Debt debt = new Debt();
        debt.setUser(user);
        debt.setLenderName(debtDto.getLenderName());
        debt.setInitialAmount(debtDto.getInitialAmount());
        debt.setDueDate(debtDto.getDueDate());

        debt.setRemainingAmount(debtDto.getInitialAmount());
        debt.setStatus(DebtStatus.ACTIVE);

        return debtRepository.save(debt);
    }

    public List<Debt> getDebtsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return debtRepository.findByUserId(user.getId());
    }

    public List<Debt> getDebtsByStatus(String username, DebtStatus status) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return debtRepository.findByUserId(user.getId()).stream()
                .filter(debt -> debt.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Debt getDebtByIdAndUsername(Long id, String username) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utang tidak ditemukan dengan ID: " + id));
        if (!debt.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Anda tidak memiliki izin untuk mengakses utang ini.");
        }
        return debt;
    }

    @Transactional
    public void updateDebt(Long id, Debt updatedDebtData, String username) {
        Debt existingDebt = getDebtByIdAndUsername(id, username);

        existingDebt.setLenderName(updatedDebtData.getLenderName());
        existingDebt.setInitialAmount(updatedDebtData.getInitialAmount());
        existingDebt.setRemainingAmount(updatedDebtData.getRemainingAmount());
        existingDebt.setDueDate(updatedDebtData.getDueDate());
        existingDebt.setStatus(updatedDebtData.getStatus());

        debtRepository.save(existingDebt);
    }

    public BigDecimal getTotalRemainingDebt(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        return debtRepository.findByUserId(user.getId()).stream()
                .map(Debt::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void deleteDebt(Long id, String username) {
        Debt debt = debtRepository.findById(id).orElseThrow();
        if (!debt.getUser().getUsername().equals(username))
            throw new AccessDeniedException("Access denied");
        debtRepository.delete(debt);
    }
}
