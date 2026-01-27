package com.example.datalib.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.datalib.user.UserDao
import com.example.datalib.user.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
 abstract class AppLocalDatabase : RoomDatabase() {
     abstract fun userDao(): UserDao
}

