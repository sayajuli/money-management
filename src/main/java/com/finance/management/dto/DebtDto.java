package com.finance.management.dto;

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

  private LocalDate dueDate;
}
