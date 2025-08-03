package com.finance.management.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class PasswordChangeDto {
  @NotEmpty(message = "Password lama tidak boleh kosong")
  private String oldPassword;

  @NotEmpty(message = "Password baru tidak boleh kosong")
  @Size(min = 6, message = "Password baru minimal 6 karakter")
  private String newPassword;

  @NotEmpty(message = "Konfirmasi password tidak boleh kosong")
  private String confirmPassword;

  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }
}
