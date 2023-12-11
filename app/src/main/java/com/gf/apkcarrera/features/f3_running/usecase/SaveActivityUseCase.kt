package com.gf.apkcarrera.features.f3_running.usecase

import android.graphics.Bitmap
import com.gf.apkcarrera.features.f3_running.repository.RunningRepository
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.response.GenericResponse
import javax.inject.Inject

class SaveActivityUseCase @Inject constructor(private val runningRepository: RunningRepository) {
    suspend operator fun invoke(activityModel: ActivityModel,imagesBitmap:List<Bitmap>) : GenericResponse = runningRepository.saveActivity(activityModel,imagesBitmap)

}
