package com.finance.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.finance.management.model.AssetType;

@Data
public class AssetDto {

    @NotNull(message = "Tipe aset tidak boleh kosong")
    private AssetType type; // Dipilih dari dropdown (CASH, INVESTMENT, PROPERTY)

    @NotBlank(message = "Nama aset tidak boleh kosong")
    private String name; // Contoh: "Tabungan BCA", "Saham BBCA", "Rumah Tinggal"

    @NotNull(message = "Nilai aset tidak boleh kosong")
    @Positive(message = "Nilai aset harus positif")
    private BigDecimal currentValue;

    @PastOrPresent(message = "Tanggal perolehan tidak boleh di masa depan")
    private LocalDate acquisitionDate; // Opsional
}
