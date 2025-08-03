package com.finance.management.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.management.service.TransactionService;

@RestController
@RequestMapping("/api")
public class ApiController {
  @Autowired
  private TransactionService transactionService;

  @GetMapping("/expense-summary")
  public Map<String, BigDecimal> getExpenseSummary(Principal principal) {
    return transactionService.getExpenseSummaryByCategory(principal.getName());
  }
}
