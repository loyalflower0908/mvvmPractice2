package com.loyalflower.focustimer.domain.usecase

import com.loyalflower.focustimer.domain.model.Resource
import com.loyalflower.focustimer.domain.model.TimerSessionModel
import com.loyalflower.focustimer.domain.repository.LocalStorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTimerSessionByDateUseCase@Inject constructor(
    private val repository: LocalStorageRepository
) {
    operator fun invoke(date:String): Flow<Resource<TimerSessionModel>> = flow {
        try {
            emit(Resource.Loading())
            emit(
                Resource.Success(
                data = repository.getTimerSessionByDate(date)
            ))
        } catch (e:Exception){
            emit(Resource.Error(e.message ?: "Unknown Error"))
        }
    }
}