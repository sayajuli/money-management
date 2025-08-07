package com.finance.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.management.model.Budget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
  List<Budget> findByUser_IdAndBudgetYearAndBudgetMonth(Long userId, int year, int month);
  Optional<Budget> findByCategoryAndUser_IdAndBudgetYearAndBudgetMonth(String category, Long userId, int year, int month);
}
