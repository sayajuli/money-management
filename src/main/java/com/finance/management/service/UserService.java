package com.finance.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.management.dto.PasswordChangeDto;
import com.finance.management.dto.UserRegistrationDto;
import com.finance.management.model.User;
import com.finance.management.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Mendaftarkan pengguna baru ke sistem.
     * 
     * @param registrationDto Data pengguna dari form registrasi.
     * @return Entitas User yang baru disimpan.
     * @throws RuntimeException jika username atau email sudah ada.
     */
    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) {
        // 1. Validasi apakah username atau email sudah ada
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Error: Username sudah digunakan!");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Error: Email sudah digunakan!");
        }

        // 2. Buat entitas User baru
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());

        // 3. Enkripsi password sebelum disimpan
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        // 4. Simpan ke database
        return userRepository.save(user);
    }

    /**
     * Mencari user berdasarkan username.
     * 
     * @param username Nama pengguna.
     * @return Entitas User.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional
    public void changePassword(String username, PasswordChangeDto dto) {
        // 1. Ambil data pengguna dari database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // 2. Verifikasi apakah password lama yang dimasukkan cocok
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Password lama salah.");
        }

        // 3. Verifikasi apakah password baru dan konfirmasinya sama
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password baru dan konfirmasi tidak cocok.");
        }

        // 4. Enkripsi password baru dan simpan ke database
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}
