package com.example.tubespm.di

import android.content.Context // Pastikan import ini ada
import com.example.tubespm.data.repository.QuizRepositoryImpl
import com.example.tubespm.repository.ActivityRepository
import com.example.tubespm.repository.ActivityRepositoryImpl
import com.example.tubespm.repository.ExerciseCatalogRepository
import com.example.tubespm.repository.ExerciseCatalogRepositoryImpl
import com.example.tubespm.repository.QuizRepository
import com.example.tubespm.repository.UserRepository
import com.example.tubespm.repository.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext // Penting
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideExerciseCatalogRepository(db: FirebaseFirestore): ExerciseCatalogRepository {
        return ExerciseCatalogRepositoryImpl(db)
    }

    // --- BAGIAN INI YANG DIPERBAIKI ---
    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore,
        // Kita hapus 'storage' dari sini karena Impl sudah tidak pakai
        @ApplicationContext context: Context // Kita tambah Context
    ): UserRepository {
        // Urutan parameter harus sama persis dengan constructor UserRepositoryImpl
        return UserRepositoryImpl(auth, db, context)
    }
    // ----------------------------------

    @Provides
    @Singleton
    fun provideActivityRepository(
        db: FirebaseFirestore,
        auth: FirebaseAuth
    ): ActivityRepository {
        return ActivityRepositoryImpl(db, auth)
    }

    @Provides
    @Singleton
    fun provideQuizRepository(db: FirebaseFirestore): QuizRepository {
        return QuizRepositoryImpl(db)
    }
}