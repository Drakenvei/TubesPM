
# 📘 Aplikasi Tryout & Latihan Soal

## 📝 Deskripsi Singkat
Aplikasi ini adalah platform mobile berbasis **Kotlin (Native Android)** yang menyediakan fitur **tryout** dan **latihan soal** yang dikerjakan untuk siswa beserta dengan pembahasannya. Selain itu, terdapat peran Admin untuk mengelola paket **Tryout** dan **Latihan Soal**.

---

## 👥 Anggota Kelompok
| Nama | NIM |
|------|-----|
| Putera Nami Shiddieqy | 231402003 |
| Nicholas Ken Surya | 231402072 |
| Kenward Keene Hermanto | 231404081 |
| Wynn Thomas Salim | 231402120 |
| William Benediktus | 231402091 |

---

## 🚀 Rencana Fitur Aplikasi

### 🎓 Fitur untuk **Siswa**
- **Register & Login**: Membuat akun dan masuk ke aplikasi.  
- **List Tryout**: Melihat daftar tryout yang tersedia.  
- **Search Tryout**: Mencari tryout.  
- **Mengerjakan Tryout**: Mengerjakan soal tryout secara online.  
- **Analisis & Skor Tryout**: Menyimpan jawaban, menampilkan skor & pembahasan.  
- **List Latihan Soal**: Melihat daftar latihan soal.  
- **Search Latihan Soal**: Mempermudah pencarian latihan soal.  
- **Mengerjakan Latihan Soal**: Dapat mengerjakan latihan soal secara online.  
- **Analisis & Skor Latihan Soal**: Menampilkan hasil & pembahasan.  
- **Edit Profil**: Mengubah data diri (nama, telepon, sekolah/kampus tujuan, jurusan, kelas).  
- **Logout**: Keluar dari halaman siswa.  

---

### 🛠️ Fitur untuk **Admin**
- **Login Dashboard Admin** — Admin dapat masuk ke halaman dashboard.  
- **Manajemen Paket Tryout** — Membuat dan menghapus paket tryout.  
- **Soal Tryout** — Menambah, mengedit, dan menghapus soal serta pembahasan pada tryout.  
- **Publish Tryout & Latihan Soal** — Memublikasikan tryout dan latihan soal apabila sudah siap.  
- **Ubah Status Tryout & Latihan Soal** — Mengubah status paket soal menjadi aktif, nonaktif, atau perbaikan.  
- **Manajemen Latihan Soal** — Membuat, mengedit, menonaktifkan, dan menghapus soal serta pembahasan latihan soal.  
- **Logout** — Keluar dari dashboard admin.

---

## 📖 Deskripsi Project
- **Jenis**: Native Android Application
- **SDK/Tools**:

| Komponen | Deskripsi |
|-----------|----------|
| **Bahasa Pemrograman** | Kotlin |
| **IDE** | Android Studio Narwhal 3 Feature Drop (2025.1.3) |
| **Android SDK** | API Level 35 |
| **JDK** | JDK 23.0.2 |
| **Architecture Pattern** | MVVM |
| **UI Framework** | Jetpack Compose |
| **Database Utama** | **Firebase Firestore** |

---

## 🧩 Cara Instalasi (Melalui Android Studio GUI)

### 1️⃣ Clone Repository dari GitHub
1. Buka **Android Studio**  
2. Pilih menu **File → New → Project from Version Control...**  
3. Pilih **Git** pada pilihan Version Control  
4. Masukkan URL repository GitHub kamu (contoh):  
   ```
   https://github.com/Drakenvei/TubesPM.git
   ```
5. Tentukan lokasi penyimpanan project di lokal, lalu klik **Clone**

---

### 2️⃣ Hubungkan ke Firebase
1. Buka **Firebase Console**: [https://console.firebase.google.com](https://console.firebase.google.com)  
2. Buat **Project Baru** → tambahkan aplikasi Android  
3. Masukkan nama package (misal `com.example.tubespm`)  
4. Unduh file `google-services.json`  
5. Letakkan file tersebut ke dalam folder:
   ```
   app/google-services.json
   ```

💡 **Catatan:**  
File `google-services.json` **tidak akan ter-upload ke GitHub** karena sudah tercantum di `.gitignore`.  
Artinya, setiap anggota tim harus **mengunduh file tersebut sendiri** dari Firebase Console proyek yang sama.

---

### 3️⃣ Sinkronisasi dan Jalankan
1. Setelah file `google-services.json` dimasukkan, buka **Gradle panel** lalu klik **Sync Project**  
2. Jika tidak ada error, tekan tombol ▶ **Run** di Android Studio  
3. Pilih emulator atau perangkat fisik → aplikasi siap dijalankan 🎉


---

