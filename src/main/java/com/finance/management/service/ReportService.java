package com.finance.management.service;

import com.finance.management.model.Transaction;
import com.finance.management.model.TransactionType;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ReportService {

    @Autowired
    private TransactionService transactionService;

    public void generatePdfReport(String username, int year, int month, HttpServletResponse response)
            throws IOException, DocumentException {
        List<Transaction> transactions = transactionService.getTransactionsForMonth(username, year, month);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME).map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE).map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netResult = totalIncome.subtract(totalExpense);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
        String monthName = YearMonth.of(year, month)
                .format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("id", "ID")));
        Paragraph title = new Paragraph("Laporan Keuangan - " + monthName, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        document.add(new Paragraph("Ringkasan Bulan Ini:"));
        document.add(new Paragraph("  - Total Pemasukan: " + formatCurrency(totalIncome)));
        document.add(new Paragraph("  - Total Pengeluaran: " + formatCurrency(totalExpense)));
        Font netFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12,
                netResult.compareTo(BigDecimal.ZERO) >= 0 ? BaseColor.GREEN.darker() : BaseColor.RED);
        Paragraph netParagraph = new Paragraph("  - Hasil Bersih: " + formatCurrency(netResult), netFont);
        netParagraph.setSpacingAfter(25);
        document.add(netParagraph);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2f, 3f, 3f, 4f });

        addTableHeader(table);

        for (Transaction tx : transactions) {
            table.addCell(formatDate(tx.getTransactionDate()));
            table.addCell(tx.getCategory());
            table.addCell(tx.getType().name());
            PdfPCell amountCell = new PdfPCell(new Phrase(formatCurrency(tx.getAmount())));
            amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(amountCell);
        }
        document.add(table);

        document.close();
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = { "Tanggal", "Kategori", "Tipe", "Jumlah" };
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        headerFont.setColor(BaseColor.WHITE);

        for (String headerTitle : headers) {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.BLUE);
            header.setBorderWidth(1);
            header.setPhrase(new Phrase(headerTitle, headerFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(5);
            table.addCell(header);
        }
    }

    private String formatCurrency(BigDecimal value) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return currencyFormat.format(value);
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
