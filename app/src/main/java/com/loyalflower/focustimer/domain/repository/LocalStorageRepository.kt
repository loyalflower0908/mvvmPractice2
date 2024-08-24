package com.loyalflower.focustimer.domain.repository

import com.loyalflower.focustimer.domain.model.TimerSessionModel

interface LocalStorageRepository {
    suspend fun saveTimerSession(timerSessionModel:TimerSessionModel):Boolean

    suspend fun getTimerSessionByDate(date:String):TimerSessionModel
}