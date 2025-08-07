package com.finance.management.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.finance.management.dto.PasswordChangeDto;
import com.finance.management.dto.ProfileUpdateDto;
import com.finance.management.model.RiskProfile;
import com.finance.management.model.User;
import com.finance.management.repository.UserRepository;
import com.finance.management.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @GetMapping
  public String showProfilePage(Model model, Principal principal) {
    User user = userRepository.findByUsername(principal.getName()).orElseThrow();

    // Menyiapkan DTO untuk form update profil
    ProfileUpdateDto profileDto = new ProfileUpdateDto();
    profileDto.setName(user.getName());
    profileDto.setEmail(user.getEmail());
    profileDto.setRiskProfile(user.getRiskProfile());
    model.addAttribute("profileDto", profileDto);

    model.addAttribute("user", user);

    // Menyiapkan DTO kosong untuk form ubah password
    model.addAttribute("passwordChangeDto", new PasswordChangeDto());

    model.addAttribute("riskProfiles", RiskProfile.values());

    return "profile";
  }

  @PostMapping("/update")
  public String updateProfile(@Valid @ModelAttribute("profileDto") ProfileUpdateDto dto,
      BindingResult result,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profileDto", result);
      redirectAttributes.addFlashAttribute("profileDto", dto);
      return "redirect:/profile";
    }
    userService.updateProfile(principal.getName(), dto);
    redirectAttributes.addFlashAttribute("successMessage", "Profil berhasil diperbarui!");
    return "redirect:/profile";
  }

  @PostMapping("/change-password")
  public String changePassword(@Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto dto,
      BindingResult result,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordChangeDto", result);
      redirectAttributes.addFlashAttribute("passwordChangeDto", dto);
      return "redirect:/profile";
    }

    try {
      userService.changePassword(principal.getName(), dto);
      redirectAttributes.addFlashAttribute("successMessage", "Password berhasil diubah!");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      redirectAttributes.addFlashAttribute("passwordChangeDto", dto);
    }

    return "redirect:/profile";
  }
}
