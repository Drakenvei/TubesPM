package com.example.tubespm.data.model

data class QuizQuestion(
    val id: Int,
    val subtest: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

fun sampleQuizQuestions() = listOf(
    QuizQuestion(1, "Tes Penalaran Umum", "Manakah urutan angka pi (π) yang benar?", listOf("3,1415926535", "3,1415926538", "3,1415926531", "3,1415926539", "3,1415926530"), 0),
    QuizQuestion(2, "Tes Penalaran Umum", "Jika semua A adalah B, dan beberapa C adalah A, maka...", listOf("Semua C adalah B", "Beberapa C adalah B", "Tidak ada C yang B", "Semua B adalah C", "Tidak dapat disimpulkan"), 1),
    QuizQuestion(3, "Tes Penalaran Umum", "Lawan kata dari 'ABSTRAK' adalah...", listOf("Nyata", "Ghaib", "Imajinasi", "Konkret", "Absurd"), 3),
    QuizQuestion(4, "Tes Penalaran Umum", "Soal nomor 4", listOf("A", "B", "C", "D", "E"), 0),
    QuizQuestion(5, "Tes Penalaran Umum", "Soal nomor 5", listOf("A", "B", "C", "D", "E"), 1),
    QuizQuestion(6, "Tes Penalaran Umum", "Soal nomor 6", listOf("A", "B", "C", "D", "E"), 2),
    QuizQuestion(7, "Tes Penalaran Umum", "Soal nomor 7", listOf("A", "B", "C", "D", "E"), 3),
    QuizQuestion(8, "Tes Penalaran Umum", "Soal nomor 8", listOf("A", "B", "C", "D", "E"), 4),
    QuizQuestion(9, "Tes Penalaran Umum", "Soal nomor 9", listOf("A", "B", "C", "D", "E"), 0)
)