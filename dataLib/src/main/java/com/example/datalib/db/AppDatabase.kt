package com.example.datalib.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserDbEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        const val NAME = "ble_platform.db"
    }
}

