package com.loyalflower.focustimer.di

import android.content.Context
import androidx.room.Room
import com.loyalflower.focustimer.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext:Context): AppDatabase{
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "focus_timer_database").build()
    }

    @Singleton
    @Provides
    fun provideTimerSessionDao(db:AppDatabase) = db.timerSessionDao()
}