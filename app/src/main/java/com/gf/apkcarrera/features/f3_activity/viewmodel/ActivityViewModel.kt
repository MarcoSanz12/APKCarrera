package com.gf.apkcarrera.features.f3_activity.viewmodel

import androidx.lifecycle.viewModelScope
import com.gf.common.entity.ActivityStatus
import com.gf.common.entity.ActivityUIState
import com.gf.common.platform.BaseViewModel
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ActivityViewModel : BaseViewModel() {

    private val _uiStateFlow = MutableStateFlow(ActivityUIState(
        time = 0,
        distance = 0,
        speedLastKm = 0.0,
        points = emptyList(),
        status = ActivityStatus.LOCATING
    ))
    val uiStateFlow = _uiStateFlow.asStateFlow()

    private val _serviceStateFlow = MutableStateFlow(ActivityStatus.LOCATING)
    val serviceStateFlow = _serviceStateFlow.asStateFlow()

    fun updateUi(newUIState: ActivityUIState){
        viewModelScope.launch(Default) {
            _uiStateFlow.value = newUIState
        }
    }

    fun updateServiceState(newState:ActivityStatus){
        viewModelScope.launch(Default) {
            _serviceStateFlow.value = newState
        }
    }
}