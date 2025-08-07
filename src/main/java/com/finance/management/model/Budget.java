package com.finance.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotEmpty(message = "Kategori tidak boleh kosong")
    @Column(nullable = false)
    private String category;

    @NotNull(message = "Jumlah anggaran tidak boleh kosong")
    @Positive(message = "Jumlah harus lebih dari nol")
    @Column(nullable = false)
    private BigDecimal amount;

    @NotNull
    @Column(nullable = false)
    private int budgetYear;

    @NotNull
    @Column(nullable = false)
    private int budgetMonth;
}