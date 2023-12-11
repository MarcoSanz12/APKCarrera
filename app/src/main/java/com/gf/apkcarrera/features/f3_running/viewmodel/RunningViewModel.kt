package com.gf.apkcarrera.features.f3_running.viewmodel

import android.graphics.Bitmap
import com.gf.apkcarrera.features.f3_running.usecase.SaveActivityUseCase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.activity.ActivityModelSimple
import com.gf.common.platform.BaseViewModel
import com.gf.common.response.GenericResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class RunningViewModel @Inject constructor(
    val saveActivityUseCase: SaveActivityUseCase
): BaseViewModel() {

    private val _activityModelSimple = MutableStateFlow<ActivityModelSimple?>(null)
    val activityModelSimple get() = _activityModelSimple.asStateFlow()

    private val _uploadedActivityResponse = MutableStateFlow<GenericResponse?>(null)
    val uploadedActivityResponse = _uploadedActivityResponse.asStateFlow()

    fun postActivityModelSimple(activityModelSimple: ActivityModelSimple) =
        launch {
            _activityModelSimple.emit(activityModelSimple)
        }

    fun uploadActivityModel(activityModel: ActivityModel,imagesBitmap : List<Bitmap>) =
        launch {
            _uploadedActivityResponse.emit(saveActivityUseCase(activityModel,imagesBitmap))
        }
}