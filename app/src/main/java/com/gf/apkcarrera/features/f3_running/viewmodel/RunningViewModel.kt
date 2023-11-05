package com.gf.apkcarrera.features.f3_running.viewmodel

import com.gf.common.entity.activity.ActivityModelSimple
import com.gf.common.platform.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class RunningViewModel : BaseViewModel() {

    private val _activityModelSimple = MutableStateFlow<ActivityModelSimple?>(null)
    val activityModelSimple get() = _activityModelSimple.asStateFlow()

    fun postActivityModelSimple(activityModelSimple: ActivityModelSimple) =
        launch {
            _activityModelSimple.emit(activityModelSimple)
        }
}