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
    model.addAttribute("user", user);
    model.addAttribute("passwordChangeDto", new PasswordChangeDto());
    return "profile";
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
