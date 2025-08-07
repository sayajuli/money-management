package com.finance.management.controller;

import com.finance.management.model.User;
import com.finance.management.repository.UserRepository;
import com.finance.management.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showReportPage(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("selectedMonth", LocalDate.now().getMonthValue());
        model.addAttribute("selectedYear", LocalDate.now().getYear());

        List<Integer> years = IntStream.rangeClosed(LocalDate.now().getYear() - 5, LocalDate.now().getYear())
                .boxed().sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList());
        model.addAttribute("years", years);

        return "reports";
    }

    @GetMapping("/generate")
    public void generateReport(@RequestParam("year") int year,
            @RequestParam("month") int month,
            Principal principal,
            HttpServletResponse response) {

        try {
            response.setContentType("application/pdf");
            String monthName = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM"));
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Laporan_" + monthName + "_" + year + ".pdf";
            response.setHeader(headerKey, headerValue);

            reportService.generatePdfReport(principal.getName(), year, month, response);

        } catch (Exception e) {
            response.setContentType("text/plain");
            try {
                response.getWriter().write("Gagal membuat laporan PDF: " + e.getMessage());
            } catch (IOException ioException) {
            }
        }
    }
}