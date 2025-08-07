package com.finance.management.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DebtDto {

    @NotBlank(message = "Nama pemberi utang tidak boleh kosong")
    private String lenderName;

    @NotNull(message = "Jumlah awal utang tidak boleh kosong")
    @Positive(message = "Jumlah utang harus positif")
    private BigDecimal initialAmount;

    // ===== FIELD BARU DI SINI =====
    private BigDecimal monthlyInstallment;

    @Min(value = 1, message = "Tanggal harus antara 1 dan 31")
    @Max(value = 31, message = "Tanggal harus antara 1 dan 31")
    private Integer dueDayOfMonth;

    private LocalDate dueDate; // Ini menjadi deadline akhir
}
