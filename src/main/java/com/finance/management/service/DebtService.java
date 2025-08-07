package com.finance.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
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

        // 1. Buat objek Debt (entity) yang baru dan kosong
        Debt newDebt = new Debt();

        // 2. Pindahkan semua data dari DTO ke Entity
        newDebt.setUser(user);
        newDebt.setLenderName(debtDto.getLenderName());
        newDebt.setInitialAmount(debtDto.getInitialAmount());
        newDebt.setDueDate(debtDto.getDueDate());
        // ===== MEMINDAHKAN DATA BARU =====
        newDebt.setMonthlyInstallment(debtDto.getMonthlyInstallment());
        newDebt.setDueDayOfMonth(debtDto.getDueDayOfMonth());

        // 3. Terapkan logika bisnis
        newDebt.setRemainingAmount(debtDto.getInitialAmount());
        newDebt.setStatus(DebtStatus.ACTIVE);

        // 4. Simpan objek ENTITY (Debt) yang sudah lengkap ke database
        return debtRepository.save(newDebt);
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
        existingDebt.setMonthlyInstallment(updatedDebtData.getMonthlyInstallment());
        existingDebt.setDueDayOfMonth(updatedDebtData.getDueDayOfMonth());

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

    public Page<Debt> findPaginatedByFilter(String username, int year, int month, DebtStatus status, int pageNo,
            int pageSize) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by("dueDate").descending());

        boolean isMonthFilterActive = (month != 0);
        boolean isYearFilterActive = (year != 0);
        boolean isStatusFilterActive = (status != null);

        // Logika untuk menangani filter "Semua"
        if (isMonthFilterActive && isYearFilterActive) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();
            if (isStatusFilterActive) {
                return debtRepository.findByUserIdAndStatusAndDueDateBetween(user.getId(), status, startDate, endDate,
                        pageable);
            } else {
                return debtRepository.findByUserIdAndDueDateBetween(user.getId(), startDate, endDate, pageable);
            }
        } else { // Jika bulan atau tahun adalah "Semua"
            if (isStatusFilterActive) {
                return debtRepository.findByUserIdAndStatus(user.getId(), status, pageable);
            } else {
                return debtRepository.findByUserId(user.getId(), pageable);
            }
        }
    }

        public BigDecimal getTotalMonthlyInstallment(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        return debtRepository.findByUserId(user.getId()).stream()
                // Filter hanya utang yang statusnya ACTIVE
                .filter(debt -> debt.getStatus() == DebtStatus.ACTIVE)
                // Ambil nilai cicilan bulanannya
                .map(Debt::getMonthlyInstallment)
                // Saring jika ada nilai yang null
                .filter(Objects::nonNull)
                // Jumlahkan semuanya
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
