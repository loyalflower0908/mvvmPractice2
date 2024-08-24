package com.loyalflower.focustimer.presentation.home

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.os.CountDownTimer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyalflower.focustimer.core.Constants.Companion.ONE_HOUR_IN_MIN
import com.loyalflower.focustimer.core.Constants.Companion.ONE_MIN_IN_MILLIS
import com.loyalflower.focustimer.core.Constants.Companion.ONE_MIN_IN_SEC
import com.loyalflower.focustimer.core.Constants.Companion.ONE_SEC_IN_MILLIS
import com.loyalflower.focustimer.domain.model.Resource
import com.loyalflower.focustimer.domain.model.TimerSessionModel
import com.loyalflower.focustimer.domain.model.TimerTypeEnum
import com.loyalflower.focustimer.domain.usecase.GetTimerSessionByDateUseCase
import com.loyalflower.focustimer.domain.usecase.SaveTimerSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val getTimerSessionByDateUseCase: GetTimerSessionByDateUseCase,
    private val saveTimerSessionUseCase: SaveTimerSessionUseCase
) : ViewModel() {
    private lateinit var timer: CountDownTimer

    private var isTimerStart: Boolean = false

    private val _isTimerActive = mutableStateOf(false)
    val isTimerActiveState = _isTimerActive

    private val _timerValue = mutableStateOf(TimerTypeEnum.FOCUS.titleToMillis())
    val timerValueState = _timerValue

    private val _timerTypeState = mutableStateOf(TimerTypeEnum.FOCUS)
    val timerTypeState = _timerTypeState

    private val _roundsState = mutableStateOf(0)
    val roundsState = _roundsState

    private val _todayTimeState = mutableStateOf<Long>(0)
    val todayTimeState = _todayTimeState

    var _sessionTimerValue: Long = 0

    fun onStartTimer() {
        viewModelScope.launch {
            if (!isTimerStart) {
                timer = object : CountDownTimer(
                    _timerValue.value,
                    ONE_SEC_IN_MILLIS
                ) {
                    override fun onTick(millisUntilFinished: Long) {
                        _timerValue.value = millisUntilFinished
                        _todayTimeState.value += ONE_SEC_IN_MILLIS
                        _sessionTimerValue += ONE_SEC_IN_MILLIS
                    }

                    override fun onFinish() {
                        onCancleTimer()
                    }
                }
                timer.start().also {
                    if (!_isTimerActive.value) _roundsState.value += 1
                    _sessionTimerValue = 0
                    _isTimerActive.value = true
                    isTimerStart = true
                }
            } else {
                timer = object : CountDownTimer(
                    _timerValue.value, ONE_SEC_IN_MILLIS
                ){
                    override fun onTick(millisUntilFinished: Long) {
                        _timerValue.value = millisUntilFinished
                        _todayTimeState.value += ONE_SEC_IN_MILLIS
                        _sessionTimerValue += ONE_SEC_IN_MILLIS
                    }

                    override fun onFinish() {
                        onCancleTimer()
                    }
                }
                timer.start().also {
                    _isTimerActive.value = true
                }
            }
        }
    }

    fun onCancleTimer(reset: Boolean = false) {
        try {
            saveTimerSession()
            timer.cancel()
        } catch (_: UninitializedPropertyAccessException) {
            //error
        }
        if (!_isTimerActive.value || reset) {
            _timerValue.value = _timerTypeState.value.titleToMillis()
            isTimerStart = false
        }
        _isTimerActive.value = false
    }

    private fun onResetTimer() {
        if (_isTimerActive.value) {
            onCancleTimer()
            onStartTimer()
        }
    }

    fun onUpdateType(timerType: TimerTypeEnum) {
        _timerTypeState.value = timerType
        onCancleTimer(reset = true)
    }

    fun onIncreaseTime() {
        _timerValue.value += ONE_MIN_IN_MILLIS
        onResetTimer()
    }

    fun onDecreaseTime() {
        _timerValue.value -= ONE_MIN_IN_MILLIS
        onResetTimer()
        if (_timerValue.value < 0) {
            onCancleTimer()
        }
    }

    fun getTimerSessionByDate() {
        getTimerSessionByDateUseCase(date = getCurrentDate()).onEach { result ->
            if (result is Resource.Success) {
                _roundsState.value = result.data?.round ?: 0
                _todayTimeState.value = result.data?.value ?: 0
            }
        }.launchIn(viewModelScope)
    }

    private fun saveTimerSession() {
        val session = TimerSessionModel(
            date = getCurrentDate(),
            value = _sessionTimerValue
        )
        saveTimerSessionUseCase(timerSessionModel = session).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _sessionTimerValue = 0
                }

                is Resource.Loading -> {}
                is Resource.Error -> {}
            }
        }.launchIn(viewModelScope)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val currentDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MMMM-yyyy")
        return formatter.format(currentDate)
    }

    @SuppressLint("DefaultLocale")
    fun millisToMinutes(value: Long): String {
        val totalSeconds = value / ONE_SEC_IN_MILLIS
        val minutes = (totalSeconds / ONE_MIN_IN_SEC).toInt()
        val seconds = (totalSeconds % ONE_MIN_IN_SEC).toInt()
        return String.format("%02d:%02d", minutes, seconds)

    }

    @SuppressLint("DefaultLocale")
    fun millisToHours(value: Long): String {
        val totalSeconds = value / ONE_SEC_IN_MILLIS
        val seconds = (totalSeconds % ONE_MIN_IN_SEC).toInt()
        val totalMinutes = (totalSeconds / ONE_MIN_IN_SEC).toInt()
        val hours = (totalMinutes / ONE_HOUR_IN_MIN)
        val minutes = (totalMinutes % ONE_HOUR_IN_MIN)
        return if (totalMinutes < ONE_HOUR_IN_MIN) {
            String.format("%02dm %02ds", minutes, seconds)
        } else {
            String.format("%02dh %02dm", hours, minutes)
        }
    }
}

