package com.example.datalib.db

import android.content.Context
import androidx.room.Room
import com.example.datalib.user.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppLocalDatabase =
        Room.databaseBuilder(
            context,
            AppLocalDatabase::class.java,
            "app_database"
        ).build()

    @Provides
    fun provideUserDao(
        db: AppLocalDatabase
    ): UserDao = db.userDao()
}
