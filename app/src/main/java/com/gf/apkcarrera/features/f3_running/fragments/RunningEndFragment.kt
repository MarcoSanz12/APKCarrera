package com.gf.apkcarrera.features.f3_running.fragments

import android.graphics.Bitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg03RunningEndBinding
import com.gf.apkcarrera.features.f3_running.adapter.RunningImagesAdapter
import com.gf.apkcarrera.features.f3_running.fragments.RunningFragment.Companion.setSpeed
import com.gf.apkcarrera.features.f3_running.viewmodel.RunningViewModel
import com.gf.common.entity.activity.ActivityModelSimple
import com.gf.common.entity.activity.RegistryPoint
import com.gf.common.extensions.assignAnimatedAdapter
import com.gf.common.extensions.collectFlow
import com.gf.common.extensions.format
import com.gf.common.extensions.toast
import com.gf.common.platform.BaseCameraFragment
import com.gf.common.utils.Constants
import com.gf.common.utils.StatCounter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunningEndFragment : BaseCameraFragment<Frg03RunningEndBinding>() {

    private val runningViewModel : RunningViewModel by activityViewModels()
    private lateinit var adapter : RunningImagesAdapter


    override fun initObservers() {
        with(runningViewModel){
            collectFlow(activityModelSimple,Lifecycle.State.STARTED,::activityLoaded)
        }
    }

    override fun initializeView() {
        with(binding){

            // Adaptador imágenes
            adapter = rvImages.assignAnimatedAdapter(RunningImagesAdapter(listOf(),::onImageClick), com.gf.common.R.anim.animation_layout_fade_in,false)

            // Añadir imagen
            cvAddImage.setOnClickListener {
                try{
                    uploadImage(195,195)

                }catch (ex: Throwable){
                    ex.printStackTrace()
                }
            }

            // Guardar carrera
            binding.btEnd.setOnClickListener {
                sendCommandToService(Constants.ACTION_END_RUNNING)
                baseActivity.navController.popBackStack(R.id.fragmentFeed,false)
                toast("Carrera guardada :D")
            }
        }
    }

    override fun onImageLoadedListener(img: Bitmap?) {
        img?.let {
            adapter.addImage(img)
        }
    }

    private fun activityLoaded(activityModelSimple: ActivityModelSimple) {
        with(binding){
            // Distancia
            statistic1.tvContentLeft.text =  (activityModelSimple.distance/1000f).format(2) + " km"

            // Tiempo
            statistic1.tvContentRight.text = StatCounter.formatTime(activityModelSimple.time.sum())

            val speed = activityModelSimple.distance.toFloat() / activityModelSimple.time.sum().toFloat()

            // Velocidad / ritmo
            statistic2.tvContentLeft.setSpeed(speed)

            // Velocidad máxima
            statistic2.tvContentRight.setSpeed(getMaxSpeed(
                activityModelSimple.distance,
                activityModelSimple.time
            ))

            // Desnivel positivo
            statistic3.tvContentLeft.text = "${getDesnivelPositivo(activityModelSimple.points)} metros"

            // Altitud máxima
            statistic3.tvContentRight.text = "${getMaxAltitude(activityModelSimple.points)} metros"
        }
    }

    private fun onImageClick(bitmaps: List<Bitmap>, i: Int) {
        (requireActivity() as MainActivity).showZoomableImage(bitmaps,i)
    }

    private fun getDesnivelPositivo(points : List<List<RegistryPoint>>) : Int{
        val flatPoints = points.flatten().sortedBy { it.altitude }
        return if (flatPoints.size < 2)
            0
        else
            flatPoints.last().altitude - flatPoints.first().altitude
    }

    private fun getMaxAltitude (points: List<List<RegistryPoint>>) : Int = points.flatten().sortedBy { it.altitude }.firstOrNull()?.altitude ?: 0

    private fun getMaxSpeed(distance : Int, times : List<Int>) : Float{

        var maxSpeed = 0f
        for (kmTime in times){
            val speed =
                if (times.lastOrNull() == kmTime)
                    (distance % 1000).toFloat()/kmTime
                else
                    1000f/kmTime

            if (speed > maxSpeed)
                maxSpeed = speed

        }

        return maxSpeed
    }



    private fun sendCommandToService(action: String) =
        (requireActivity() as MainActivity).sendCommandToService(action)

}