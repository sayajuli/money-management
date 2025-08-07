package com.finance.management.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.finance.management.model.Asset;
import com.finance.management.model.AssetType;
import com.finance.management.model.User;
import com.finance.management.repository.UserRepository;
import com.finance.management.service.AssetService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/assets")
public class AssetController {

  @Autowired
  private AssetService assetService;

  @Autowired
  private UserRepository userRepository;

  @GetMapping
  public String showAssetPage(@RequestParam(required = false) AssetType type,
      @RequestParam(required = false) Integer month,
      @RequestParam(required = false) Integer year,
      @RequestParam(defaultValue = "1") int page,
      Model model, Principal principal) {
    User user = userRepository.findByUsername(principal.getName()).orElseThrow();
    String username = principal.getName();
    int pageSize = 10;
    int currentMonth = (month == null) ? LocalDate.now().getMonthValue() : month;
    int currentYear = (year == null) ? LocalDate.now().getYear() : year;

    Page<Asset> assetPage = assetService.findPaginatedByMonth(username, currentYear, currentMonth, type, page,
        pageSize);

    model.addAttribute("user", user);
    model.addAttribute("assetPage", assetPage);
    model.addAttribute("assetTypes", AssetType.values());
    model.addAttribute("selectedType", type);
    model.addAttribute("selectedMonth", currentMonth);
    model.addAttribute("selectedYear", currentYear);

    List<Integer> years = IntStream.rangeClosed(LocalDate.now().getYear() - 5, LocalDate.now().getYear())
        .boxed().sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList());
    model.addAttribute("years", years);

    if (!model.containsAttribute("asset")) {
      model.addAttribute("asset", new Asset());
    }

    return "assets";
  }

  @PostMapping("/add")
  public String addAsset(@Valid @ModelAttribute("asset") Asset asset,
      BindingResult result,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.asset", result);
      redirectAttributes.addFlashAttribute("asset", asset);
      return "redirect:/assets";
    }

    assetService.addOrUpdateAsset(asset, principal.getName());

    redirectAttributes.addFlashAttribute("successMessage", "Aset berhasil dicatat dan transaksi pengeluaran dibuat!");
    return "redirect:/assets";
  }

  @GetMapping("/edit/{id}")
  public String showEditAssetForm(@PathVariable Long id, Model model, Principal principal) {
    Asset asset = assetService.getAssetByIdAndUsername(id, principal.getName());
    model.addAttribute("asset", asset);
    return "edit-asset";
  }

  @PostMapping("/update/{id}")
  public String updateAsset(@PathVariable Long id,
      @Valid @ModelAttribute("asset") Asset asset,
      BindingResult result,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      asset.setId(id);
      return "edit-asset";
    }
    assetService.updateAsset(id, asset, principal.getName());
    redirectAttributes.addFlashAttribute("successMessage", "Aset berhasil diperbarui!");
    return "redirect:/assets";
  }

  @PostMapping("/delete/{id}")
  public String deleteAsset(@PathVariable Long id, Principal principal, RedirectAttributes attrs) {
    try {
      assetService.deleteAsset(id, principal.getName());
      attrs.addFlashAttribute("successMessage", "Aset berhasil dihapus!");
    } catch (Exception e) {
      attrs.addFlashAttribute("errorMessage", "Gagal menghapus: " + e.getMessage());
    }
    return "redirect:/assets";
  }
}