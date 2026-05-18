package com.example.manekelsa.di

import com.example.manekelsa.data.firebase.FirebaseWorkerDataSource
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseWorkerDataSource(): FirebaseWorkerDataSource {
        val firestore = runCatching { Firebase.firestore }.getOrNull()
        val storage = runCatching { Firebase.storage }.getOrNull()

        firestore?.let { db ->
            runCatching {
                db.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
            }
        }

        return FirebaseWorkerDataSource(
            firestore = firestore,
            storage = storage,
        )
    }
}
