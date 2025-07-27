package com.finance.management.repository;

import com.finance.management.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    Page<Transaction> findByUserIdAndCategory(Long userId, String category, Pageable pageable);
    List<Transaction> findAllByUserId(Long userId);
    List<Transaction> findAllByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
