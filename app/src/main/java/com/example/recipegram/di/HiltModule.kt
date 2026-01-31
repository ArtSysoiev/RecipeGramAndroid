package com.example.recipegram.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.recipegram.data.RecipeGramDao
import com.example.recipegram.data.RecipeGramDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class RecipeGramApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RecipeGramDatabase {
        return RecipeGramDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideDao(database: RecipeGramDatabase): RecipeGramDao {
        return database.dao()
    }
}