package com.example.datalib.di

import android.content.Context
import androidx.room.Room
import com.example.datalib.db.AppDatabase
import com.example.datalib.db.UserDao
import com.example.datalib.user.IUserRepository
import com.example.datalib.user.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Wiring for the data layer: database, DAOs and repositories.
 *
 * Consumers depend only on the interfaces ([IUserRepository]); the Room types
 * never leak past this module.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): IUserRepository =
        UserRepositoryImpl(userDao)
}

