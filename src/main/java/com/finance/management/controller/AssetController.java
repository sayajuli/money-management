package com.finance.management.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.finance.management.model.Asset;
import com.finance.management.model.AssetType;
import com.finance.management.service.AssetService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/assets")
public class AssetController {

  @Autowired
  private AssetService assetService;

  @GetMapping
  public String showAssetPage(@RequestParam(required = false) AssetType type, Model model, Principal principal) {
    String username = principal.getName();
    List<Asset> assets;

    if (type != null) {
      assets = assetService.getAssetsByType(username, type);
    } else {
      assets = assetService.getAssetsForUser(username);
    }

    model.addAttribute("assets", assets);
    model.addAttribute("assetTypes", AssetType.values());
    model.addAttribute("selectedType", type);

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