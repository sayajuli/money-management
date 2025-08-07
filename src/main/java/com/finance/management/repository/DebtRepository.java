package com.finance.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.management.model.Debt;
import com.finance.management.model.DebtStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByUserId(Long userId);

    Page<Debt> findByUserId(Long userId, Pageable pageable);
    Page<Debt> findByUserIdAndStatus(Long userId, DebtStatus status, Pageable pageable);
    Page<Debt> findByUserIdAndDueDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<Debt> findByUserIdAndStatusAndDueDateBetween(Long userId, DebtStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
