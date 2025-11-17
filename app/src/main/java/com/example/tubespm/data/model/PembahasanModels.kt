package com.example.tubespm.data.model

//data class QuestionWithExplanation(
//    val id: Int,
//    val subtest: String,
//    val questionText: String,
//    val options: List<String>,
//    val correctAnswerIndex: Int,
//    val userAnswerIndex: Int?, // Jawaban yang dipilih user
//    val explanation: String // Pembahasan soal
//)
//
//// Sample data untuk preview/testing
//fun sampleQuestionsWithExplanation() = listOf(
//    QuestionWithExplanation(
//        id = 1,
//        subtest = "Tes Penalaran Umum",
//        questionText = "Usia seorang ibu adalah tiga kali usia anaknya. Lima tahun yang lalu, usia ibu adalah empat kali usia anaknya. Berapakah usia ibu saat ini?",
//        options = listOf("39 Tahun", "45 Tahun", "40 Tahun", "49 Tahun", "42 Tahun"),
//        correctAnswerIndex = 2, // 40 Tahun
//        userAnswerIndex = 0, // User jawab 39 Tahun (salah)
//        explanation = "Misalkan usia anak saat ini adalah X tahun, maka usia ibu adalah 3X tahun.\n\nLima tahun yang lalu, usia anak adalah X - 5 tahun dan usia ibu adalah 3X - 5 tahun.\n\nDiketahui bahwa lima tahun yang lalu, usia ibu adalah empat kali usia anaknya, sehingga 3X - 5 = 4(X - 5).\n\nDengan menyelesaikan persamaan tersebut, diperoleh X = 10.\n\nJadi, usia ibu saat ini adalah 3X = 30 tahun."
//    ),
//    QuestionWithExplanation(
//        id = 2,
//        subtest = "Tes Penalaran Umum",
//        questionText = "Manakah urutan angka pi (π) yang benar?",
//        options = listOf("3,1415926535", "3,1415926538", "3,1415926531", "3,1415926539", "3,1415926530"),
//        correctAnswerIndex = 0,
//        userAnswerIndex = 0, // User jawab benar
//        explanation = "Urutan angka pi (π) yang benar hingga 10 desimal adalah 3,1415926535. Ini adalah konstanta matematis yang merepresentasikan rasio keliling lingkaran terhadap diameternya."
//    ),
//    QuestionWithExplanation(
//        id = 3,
//        subtest = "Tes Penalaran Umum",
//        questionText = "Jika semua A adalah B, dan beberapa C adalah A, maka...",
//        options = listOf("Semua C adalah B", "Beberapa C adalah B", "Tidak ada C yang B", "Semua B adalah C", "Tidak dapat disimpulkan"),
//        correctAnswerIndex = 1,
//        userAnswerIndex = null, // User tidak menjawab
//        explanation = "Dari premis 'semua A adalah B' dan 'beberapa C adalah A', kita dapat menyimpulkan bahwa beberapa C pasti adalah B. Ini karena jika beberapa C adalah A, dan semua A adalah B, maka beberapa C tersebut juga harus B."
//    )
//)