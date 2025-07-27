package com.finance.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.finance.management.model.TransactionType;

@Data
public class TransactionDto {

    // Tidak perlu ID, karena ini untuk data baru.
    // Tidak perlu User, karena akan diambil dari sesi login di service.

    @NotNull(message = "Tipe transaksi tidak boleh kosong")
    private TransactionType type; // Akan berupa dropdown INCOME atau EXPENSE di form

    @NotNull(message = "Jumlah tidak boleh kosong")
    @Positive(message = "Jumlah harus lebih dari nol")
    private BigDecimal amount;

    @NotBlank(message = "Kategori tidak boleh kosong")
    private String category;

    private String description; // Deskripsi bersifat opsional

    @NotNull(message = "Tanggal transaksi tidak boleh kosong")
    @PastOrPresent(message = "Tanggal tidak boleh di masa depan")
    private LocalDate transactionDate;
}
