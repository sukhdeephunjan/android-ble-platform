package com.example.datalib.di

import android.content.Context
import androidx.room.Room
import com.example.datalib.db.AppDatabase
import com.example.datalib.db.UserDao
import com.example.datalib.user.IUserRepository
import com.example.datalib.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ble_app_database"
        ).build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): IUserRepository = UserRepository(userDao)
}

