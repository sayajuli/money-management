# KeuanganKu - Aplikasi Manajemen Keuangan Pribadi

## 📖 Deskripsi

KeuanganKu adalah aplikasi web untuk manajemen keuangan pribadi yang membantu Anda mengelola transaksi, aset, utang, dan anggaran secara terintegrasi. Aplikasi ini dilengkapi dengan fitur analisis kesehatan keuangan dan rekomendasi finansial yang personal.

## ✨ Fitur Utama

- **Dashboard Interaktif**: Ringkasan keuangan dengan grafik dan metrik kesehatan finansial
- **Manajemen Transaksi**: Catat pemasukan dan pengeluaran dengan kategorisasi
- **Manajemen Aset**: Kelola berbagai jenis aset (Cash, Investment, Property)
- **Manajemen Utang**: Pantau utang dengan fitur pencatatan pembayaran cicilan
- **Sistem Anggaran**: Buat dan pantau anggaran per kategori dengan progress tracking
- **Laporan PDF**: Generate laporan keuangan bulanan dalam format PDF
- **Analisis Kesehatan Keuangan**: Rasio utang, tingkat tabungan, dan rekomendasi
- **Profil Risiko Investasi**: Sesuaikan rekomendasi berdasarkan profil risiko
- **Autentikasi & Keamanan**: Sistem login yang aman dengan enkripsi password

## 🛠️ Teknologi yang Digunakan

- **Backend**: Java 21, Spring Boot 3.5.3, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5.3.3, Chart.js
- **Database**: MySQL 8.0+
- **Build Tool**: Maven
- **PDF Generation**: iText 5.5.13
- **Styling**: Bootstrap Icons

## 📋 Requirements

- Java 21 atau lebih tinggi
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, atau VS Code)

## 🚀 Instalasi dan Setup

### 1. Kloning Repository

```bash
git clone <repository-url>
cd finance-management
```

### 2. Setup Database

#### Buat Database MySQL

```sql
CREATE DATABASE finance_manage;
USE finance_manage;
```

#### Jalankan Script SQL

```sql
-- Tabel untuk menyimpan data pengguna
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    risk_profile VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabel untuk mencatat transaksi keuangan
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    transaction_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabel untuk mencatat aset yang dimiliki
CREATE TABLE assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    current_value DECIMAL(15, 2) NOT NULL,
    acquisition_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabel untuk mencatat utang
CREATE TABLE debts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lender_name VARCHAR(100) NOT NULL,
    initial_amount DECIMAL(15, 2) NOT NULL,
    remaining_amount DECIMAL(15, 2) NOT NULL,
    monthly_installment DECIMAL(15, 2),
    due_day_of_month INT,
    due_date DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabel untuk menyimpan data anggaran
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    budget_year INT NOT NULL,
    budget_month INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_budget (user_id, category, budget_year, budget_month)
);
```

### 3. Konfigurasi Database

Edit file `src/main/resources/application.properties`:

```properties
spring.application.name=management

# Konfigurasi Database - sesuaikan dengan setup MySQL Anda
spring.datasource.url=jdbc:mysql://localhost:3306/finance_manage?useSSL=false&serverTimezone=Asia/Jakarta
spring.datasource.username=root
spring.datasource.password=your_mysql_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

> ⚠️ **Penting**: Ganti `your_mysql_password` dengan password MySQL Anda yang sebenarnya.

### 4. Build dan Jalankan Aplikasi

#### Menggunakan Maven Wrapper (Recommended)

```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### Menggunakan Maven

```bash
mvn clean install
mvn spring-boot:run
```

### 5. Akses Aplikasi

Buka browser dan akses: `http://localhost:8080`

## 📱 Cara Penggunaan

### 1. Registrasi dan Login

1. **Registrasi Akun Baru**:
   - Klik "Daftar di sini" di halaman login
   - Isi username, email, dan password
   - Klik "Daftar"

2. **Login**:
   - Masukkan username dan password
   - Klik "Login"

### 2. Dashboard

Setelah login, Anda akan melihat dashboard yang menampilkan:
- Ringkasan keuangan bulan ini (pemasukan, pengeluaran, aset, utang)
- Analisis kesehatan keuangan (rasio utang, tingkat tabungan)
- Pelacakan anggaran bulan ini
- Grafik pengeluaran per kategori
- Rekomendasi finansial personal

### 3. Manajemen Transaksi

**Menambah Transaksi**:
1. Pilih menu "Transaksi"
2. Isi form di sebelah kiri:
   - Pilih tipe (INCOME/EXPENSE)
   - Masukkan jumlah (otomatis terformat)
   - Isi kategori
   - Pilih tanggal
   - Tambahkan deskripsi (opsional)
3. Klik "Simpan"

**Filter Transaksi**:
- Gunakan filter bulan, tahun, dan kategori
- Klik "Filter" untuk menerapkan

**Edit/Hapus Transaksi**:
- Klik tombol mata (👁️) untuk melihat detail
- Klik tombol pensil (✏️) untuk edit
- Klik tombol sampah (🗑️) untuk hapus

### 4. Manajemen Aset

**Menambah Aset**:
1. Pilih menu "Aset"
2. Isi form:
   - Nama aset (contoh: "Tabungan BCA")
   - Pilih tipe (CASH, INVESTMENT, PROPERTY)
   - Masukkan nilai saat ini
   - Tanggal perolehan (opsional)
3. Klik "Simpan Aset"

> 💡 **Tips**: Ketika menambah aset, sistem otomatis akan membuat transaksi pengeluaran untuk "Pembelian Aset"

### 5. Manajemen Utang

**Menambah Utang**:
1. Pilih menu "Utang"
2. Isi form:
   - Nama pemberi pinjaman
   - Jumlah awal utang
   - Cicilan per bulan (opsional)
   - Tanggal jatuh tempo bulanan (opsional)
   - Tanggal jatuh tempo akhir

**Bayar Utang**:
1. Klik tombol "Bayar" pada utang yang aktif
2. Masukkan jumlah pembayaran
3. Klik "Simpan Pembayaran"

> 📝 **Catatan**: Pembayaran utang otomatis akan tercatat sebagai transaksi pengeluaran

### 6. Sistem Anggaran

**Membuat Anggaran**:
1. Pilih menu "Anggaran"
2. Pilih bulan dan tahun
3. Isi kategori dan jumlah anggaran
4. Klik "Simpan Anggaran"

**Pelacakan Anggaran**:
- Progress bar menunjukkan persentase penggunaan anggaran
- Warna hijau: < 75% terpakai
- Warna kuning: 75-90% terpakai  
- Warna merah: > 90% terpakai

### 7. Laporan PDF

1. Pilih menu "Laporan"
2. Pilih bulan dan tahun
3. Klik "Cetak Laporan"
4. File PDF akan otomatis terdownload

### 8. Pengaturan Profil

**Update Profil**:
1. Klik dropdown nama Anda di navbar
2. Pilih "Profil Saya"
3. Update nama, email, dan profil risiko investasi
4. Klik "Update Profil"

**Ubah Password**:
1. Di halaman profil, isi form ubah password
2. Masukkan password lama dan password baru
3. Klik "Simpan Password Baru"

## 💡 Tips Penggunaan

1. **Kategorisasi Konsisten**: Gunakan nama kategori yang konsisten untuk analisis yang lebih baik
2. **Update Nilai Aset**: Perbarui nilai aset secara berkala untuk tracking yang akurat
3. **Set Anggaran Realistis**: Buat anggaran berdasarkan pola pengeluaran sebelumnya
4. **Profil Risiko**: Atur profil risiko untuk mendapat rekomendasi investasi yang sesuai
5. **Backup Data**: Export laporan PDF secara berkala sebagai backup

## 🎯 Analisis Kesehatan Keuangan

### Rasio Utang terhadap Aset
- **Sehat**: < 40%
- **Perlu Perhatian**: 40-60%
- **Berisiko**: > 60%

### Tingkat Tabungan (Savings Rate)
- **Sangat Baik**: > 20%
- **Cukup**: 10-20%
- **Perlu Ditingkatkan**: < 10%
- **Boros**: Pengeluaran > Pemasukan

## 🔧 Troubleshooting

### Database Connection Error
- Pastikan MySQL service berjalan
- Cek username/password di `application.properties`
- Pastikan database `finance_manage` sudah dibuat

### Port 8080 Already in Use
```bash
# Hentikan proses yang menggunakan port 8080
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Build Error
```bash
# Clean dan rebuild
./mvnw clean install
```

## 📞 Support

Jika mengalami masalah dalam instalasi atau penggunaan:
1. Pastikan semua requirements terpenuhi
2. Cek log error di console
3. Restart aplikasi dan database

## 🔐 Keamanan

- Password di-hash menggunakan BCrypt
- Session management dengan Spring Security
- CSRF protection enabled
- SQL injection protection melalui JPA/Hibernate

---

**Selamat menggunakan KeuanganKu! 💰✨**