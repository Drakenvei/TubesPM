package com.example.tubespm.repository

import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Tryout
import kotlinx.coroutines.flow.Flow

// Interface ini mendefinisikan "apa" yang bisa dilakukan repository
interface ExerciseCatalogRepository {
    fun getTryouts(): Flow<List<Tryout>>
    fun getLatihanSoal(): Flow<List<LatihanSoal>>
}