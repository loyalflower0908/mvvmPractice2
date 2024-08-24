package com.loyalflower.focustimer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.loyalflower.focustimer.data.local.dao.TimerSessionDao
import com.loyalflower.focustimer.data.local.entity.TimerSessionEntity

@Database(entities = [TimerSessionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timerSessionDao(): TimerSessionDao
}