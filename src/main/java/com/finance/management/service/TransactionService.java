package com.finance.management.service;

import com.finance.management.model.Transaction;
import com.finance.management.model.TransactionType;
import com.finance.management.model.User;
import com.finance.management.repository.TransactionRepository;
import com.finance.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private AssetService assetService;

    public Page<Transaction> findPaginated(String username, int pageNo, int pageSize) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by("transactionDate").descending());
        return transactionRepository.findByUserId(user.getId(), pageable);
    }

    public Page<Transaction> findPaginatedByCategory(String username, String category, int pageNo, int pageSize) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by("transactionDate").descending());
        return transactionRepository.findByUserIdAndCategory(user.getId(), category, pageable);
    }

    public BigDecimal getIncomeForCurrentMonth(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        return transactionRepository.findAllByUserIdAndTransactionDateBetween(user.getId(), startOfMonth, endOfMonth)
                .stream()
                .filter(tx -> tx.getType() == TransactionType.INCOME && tx.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getExpenseForCurrentMonth(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        return transactionRepository.findAllByUserIdAndTransactionDateBetween(user.getId(), startOfMonth, endOfMonth)
                .stream()
                .filter(tx -> tx.getType() == TransactionType.EXPENSE && tx.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalIncome(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return transactionRepository.findAllByUserId(user.getId())
                .stream()
                .filter(tx -> tx.getType() == TransactionType.INCOME && tx.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpense(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return transactionRepository.findAllByUserId(user.getId())
                .stream()
                .filter(tx -> tx.getType() == TransactionType.EXPENSE && tx.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getExpenseSummaryByCategory(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return transactionRepository.findAllByUserId(user.getId())
                .stream()
                .filter(tx -> tx.getType() == TransactionType.EXPENSE && tx.getAmount() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        transaction.setUser(user);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // LOGIKA OTOMATISASI:
        if (savedTransaction.getType() == TransactionType.INCOME) {
            // Jika PEMASUKAN, tambahkan ke kas
            assetService.depositToCash(savedTransaction.getAmount(), username);
        } else if (savedTransaction.getType() == TransactionType.EXPENSE) {
            // Jika PENGELUARAN, kurangi dari kas
            assetService.withdrawFromCash(savedTransaction.getAmount(), username);
        }

        return savedTransaction;
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionByIdAndUsername(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan dengan ID: " + id));
        if (!transaction.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Anda tidak memiliki izin untuk mengakses transaksi ini.");
        }
        return transaction;
    }

    @Transactional
    public void updateTransaction(Long id, Transaction updatedTransactionData, String username) {
        Transaction existingTransaction = getTransactionByIdAndUsername(id, username);
        existingTransaction.setType(updatedTransactionData.getType());
        existingTransaction.setAmount(updatedTransactionData.getAmount());
        existingTransaction.setCategory(updatedTransactionData.getCategory());
        existingTransaction.setDescription(updatedTransactionData.getDescription());
        existingTransaction.setTransactionDate(updatedTransactionData.getTransactionDate());
        transactionRepository.save(existingTransaction);
    }

    public List<String> getUniqueCategories(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return transactionRepository.findAllByUserId(user.getId())
                .stream()
                .map(Transaction::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTransaction(Long id, String username) {
        Transaction transaction = getTransactionByIdAndUsername(id, username);
        transactionRepository.delete(transaction);
    }
}