package com.gf.apkcarrera.features.f5_profile.fragments.statistics

import android.widget.TextView
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg05ProfileStatsDataBinding
import com.gf.apkcarrera.features.f5_profile.viewmodel.ProfileViewModel
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.activity.ActivityType
import com.gf.common.extensions.collectFlowOnce
import com.gf.common.platform.BaseFragment
import com.gf.common.response.ProfileResponse
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class StatDataFragment(val activityType: ActivityType) : BaseFragment<Frg05ProfileStatsDataBinding>() {

    private val viewModel : ProfileViewModel by hiltNavGraphViewModels(R.id.nav_profile)

    private lateinit var activities : List<ActivityModel>

    private val currentTime by lazy { (System.currentTimeMillis()/1000) }

    private val weekStartTimeStamp : Long  by lazy { currentTime - 604800L  }

    private val yearStartTimeStamp : Long by lazy {
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.time.time/1000L
    }
    override fun initializeView() {
        if (this::activities.isInitialized)
            populateStatistics()
        else
            with(viewModel){
                collectFlowOnce(profileState,::profileLoaded)
            }
    }

    private fun profileLoaded(profileResponse: ProfileResponse) {
        if (profileResponse is ProfileResponse.Succesful){
            activities = profileResponse.activityList.filter { it.type == activityType }
            populateStatistics()
        }
        else
            error()

    }

    private fun populateStatistics(){
        val weekActivities = activities.filter { it.timestamp > weekStartTimeStamp }
        val yearActivities = activities.filter { it.timestamp > yearStartTimeStamp }

        with(binding){
            // 1. Actividad semanal
            placeData(weekActivities,tvCountContentWeek,tvTimeContentWeek,tvDistanceContentWeek,tvSpeedContentWeek)

            // 2. Actividad anual
            placeData(yearActivities,tvCountContentYear,tvTimeContentYear,tvDistanceContentYear,tvSpeedContentYear)

            // 3. Actividad total
            placeData(activities,tvCountContentTotal,tvTimeContentTotal,tvDistanceContentTotal,tvSpeedContentTotal)
        }
    }

    private fun placeData(listActivities : List<ActivityModel>,tvCount : TextView,tvTime : TextView, tvDistance : TextView, tvSpeed:TextView){
        // 1. Contador actividades
        tvCount.text = listActivities.size.toString()

        // 2. Tiempo
        val totalSeconds = listActivities.sumOf { it.time.sum() }
        tvTime.text =formatTime(totalSeconds)

        // 3. Distancia
        val totalMeters = listActivities.sumOf { it.distance }
        tvDistance.text = formatDistance(totalMeters)

        // 4. Velocidad media
        val averageSpeed = totalMeters/totalSeconds.toDouble()
        tvSpeed.text = formatSpeed(averageSpeed)
    }
    private fun error(){
        snackbar(com.gf.common.R.string.generic_error)
        onBackPressed()
    }

    private fun formatSpeed(speed:Double) : String{
        return when (activityType){
            ActivityType.BIKE ->{
                val kmh = (speed * 3.6).toInt()
                "$kmh km/h"
            }
            else ->{
                if (speed > 0) {
                    val speedMinsPerKilometer = ((1 / speed) / 60) * 1000

                    // Calcula los minutos y segundos
                    val minutos = (speedMinsPerKilometer).toInt()
                    val segundos = ((speedMinsPerKilometer * 60) % 60).toInt()

                    // Formatea la cadena en "mm:ss"
                    String.format("%02d:%02d m/km", minutos, segundos)
                }
                else
                    "0:00"
            }
        }
    }

    private fun formatTime(time: Int) : String{
        val hours = time / 3600
        val mins = (time % 3600) / 60

        return "${hours}h ${String.format("%02d",mins)}m"
    }

    private fun formatDistance(distance: Int) : String{
        return if (distance < 1000)
            "$distance m"
        else
            "${distance/1000} km"

    }

}