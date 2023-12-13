package com.gf.apkcarrera.features.f3_running.fragments

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg03RunningEndBinding
import com.gf.apkcarrera.features.f3_running.adapter.RunningImagesAdapter
import com.gf.apkcarrera.features.f3_running.fragments.RunningFragment.Companion.setSpeed
import com.gf.apkcarrera.features.f3_running.viewmodel.RunningViewModel
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.activity.ActivityModelSimple
import com.gf.common.entity.activity.ActivityType.*
import com.gf.common.entity.activity.RegistryField
import com.gf.common.entity.activity.RegistryPoint
import com.gf.common.extensions.adjustCamera
import com.gf.common.extensions.assignAnimatedAdapter
import com.gf.common.extensions.collectFlow
import com.gf.common.extensions.collectFlowOnce
import com.gf.common.extensions.format
import com.gf.common.extensions.paintPolyline
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseCameraFragment
import com.gf.common.response.GenericResponse
import com.gf.common.utils.Constants
import com.gf.common.utils.StatCounter
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunningEndFragment : BaseCameraFragment<Frg03RunningEndBinding>() {

    private val runningViewModel : RunningViewModel by hiltNavGraphViewModels(R.id.nav_running)
    private lateinit var adapter : RunningImagesAdapter


    companion object{
        private const val MAX_IMAGES = 10
        private const val VISIBLE_DEFAULT = true
    }

    override fun initObservers() {
        with(runningViewModel){
            collectFlow(activityModelSimple, Lifecycle.State.STARTED,::activityLoaded)
            collectFlowOnce(uploadedActivityResponse,::activitySaved)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            map.onCreate(savedInstanceState)
            binding.btVisibility.isChecked = VISIBLE_DEFAULT
            // Adaptador imágenes
            adapter = rvImages.assignAnimatedAdapter(RunningImagesAdapter(listOf(),::onImageClick,::imagesChanged), com.gf.common.R.anim.animation_layout_fade_in,false)
            (rvImages.layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL

            // Añadir imagen
            cvAddImage.setOnClickListener {
                try{
                    uploadImage()
                }catch (ex: Throwable){
                    ex.printStackTrace()
                }
            }

            binding.btVisibility.setOnClickListener(::onVisibilityClick)
        }
    }

    private fun saveActivity(activityModelSimple: ActivityModelSimple) {
        val activityModel = ActivityModel().apply {
            // Titulo
            title = binding.etTitle.text.toString()

            // Fecha guardado
            timestamp = System.currentTimeMillis()/1000

            // Tipo
            type = activityModelSimple.activityType

            // Puntos
            points = activityModelSimple.points.toRegistryFields()

            // Distancia
            distance = activityModelSimple.distance

            // Tiempo
            time = activityModelSimple.time

            // Visibiliy
            visibility = binding.btVisibility.isChecked
        }

        // Imágenes
        val imagesBitmap = adapter.resourceListFiltered.map { it.image }

        showLoadingDialog(getString(com.gf.common.R.string.saving_race))
        runningViewModel.uploadActivityModel(activityModel,imagesBitmap)
    }

    private fun activitySaved(genericResponse: GenericResponse) {
        when (genericResponse){
            is GenericResponse.Succesful -> {
                sendCommandToService(Constants.ACTION_END_RUNNING)
                baseActivity.navController.popBackStack(R.id.fragmentFeed,false)
                snackbar(com.gf.common.R.string.race_saved)
            }
            is GenericResponse.Error -> {
                snackbar(com.gf.common.R.string.race_saved_error)
            }
        }
        hideLoadingDialog()
    }

    private fun List<List<RegistryPoint>>.toRegistryFields() : List<RegistryField> {
        val registryFields = mutableListOf<RegistryField>()
        this.forEach {
            registryFields.add(RegistryField(it))
        }

        return registryFields
    }

    private fun onVisibilityClick(view: View?) {
        with (view as MaterialButton){
            binding.tvVisibility.text = if (isChecked)
                resources.getStringArray(com.gf.common.R.array.visibility_message_on).random()
            else
                resources.getStringArray(com.gf.common.R.array.visibility_message_off).random()
        }
    }

    override fun onImageLoadedListener(img: Bitmap?) {
        img?.let {
            adapter.addImage(img)

            imagesChanged()

            binding.svImagenes.apply {
                scrollX = adapter.resourceList.size + 1
            }
        }
    }
    private fun imagesChanged() = binding.cvAddImage.visible(adapter.resourceList.size < MAX_IMAGES)

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

            // Tipo actividad
            val typeResource = when(activityModelSimple.activityType){
                WALK -> com.gf.common.R.drawable.icon_walk
                RUN -> com.gf.common.R.drawable.icon_run
                BIKE -> com.gf.common.R.drawable.icon_bike
            }

            ivActivityType.setImageResource(typeResource)
            // Desnivel positivo
            statistic3.tvContentLeft.text = "${getDesnivelPositivo(activityModelSimple.points)} metros"

            // Altitud máxima
            statistic3.tvContentRight.text = "${getMaxAltitude(activityModelSimple.points)} metros"

            // Guardar carrera
            binding.btEnd.setOnClickListener {
                saveActivity(activityModelSimple)
            }

            binding.btDiscard.setOnClickListener {
                discardRace()
            }
            loadMap(activityModelSimple.points)
        }
    }

    private fun loadMap(points: List<List<RegistryPoint>>) {
        binding.map.getMapAsync{
            it.paintPolyline(requireContext(),points)
            it.adjustCamera(points)
        }
    }


    private fun onImageClick(bitmaps: List<Bitmap>, i: Int) {
        (requireActivity() as MainActivity).showZoomableImage(images = bitmaps, position = i)
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

    private fun discardRace(){
        showDeleteConfirmationDialog {
            sendCommandToService(Constants.ACTION_END_RUNNING)
            baseActivity.navController.popBackStack(R.id.fragmentFeed,false)
            snackbar(com.gf.common.R.string.race_discarded)
        }
    }

    private fun showDeleteConfirmationDialog(onConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(getString(com.gf.common.R.string.discard_race_message))
            .setPositiveButton(getString(com.gf.common.R.string.discard_yes)) { _, _ ->
                // Llamado cuando el usuario hace clic en "Sí"
                onConfirmed.invoke()
            }
            .setNegativeButton(getString(com.gf.common.R.string.discard_no), null) // No es necesario especificar una acción para "No"
            .show()
    }


    private fun sendCommandToService(action: String) =
        (requireActivity() as MainActivity).sendCommandToService(action)



}