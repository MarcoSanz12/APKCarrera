package com.gf.apkcarrera.features.f3_running.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.databinding.Frg03RunningBinding
import com.gf.apkcarrera.features.f3_running.service.RunningService
import com.gf.common.entity.ActivityStatus
import com.gf.common.entity.RunningUIState
import com.gf.common.extensions.collectFlowOnce
import com.gf.common.extensions.format
import com.gf.common.extensions.invisible
import com.gf.common.extensions.isPermissionGranted
import com.gf.common.extensions.toLatLng
import com.gf.common.extensions.toast
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseFragment
import com.gf.common.utils.Constants
import com.gf.common.utils.StatCounter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer


@AndroidEntryPoint
class RunningFragment : OnMapReadyCallback,BaseFragment<Frg03RunningBinding>() {

    private val TAG = "FragmentRunning"

    // VAR Map
    private lateinit var map: GoogleMap
    private var userLocation: Location? = null
    private var CAM_MOVING = false
    private var CAM_TRACK = true
    private lateinit var permissionToAsk: MutableSet<String>

    // VAR Actividad
    private var timer: Timer? = null
    private var recoveredState: Boolean = true
    private var STATUS: ActivityStatus = ActivityStatus.LOCATING
    private var uiPoints = listOf<LatLng>()
    private var lastLocation : LatLng? = null

    private val fusedLocationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(requireContext())

    private val locationRequest: LocationRequest
        get() = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

    private val trackingRequest: LocationRequest
        get() = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER,1000).apply {
            setMinUpdateDistanceMeters(5f)
        }.build()

    private val canUpdateCamera
        get() = (CAM_TRACK && !CAM_MOVING)


    companion object {
        private const val LOCATION_MIN_ACCURACY = 30
        private const val MIN_CAM_ZOOM = 16f
        private const val CAM_ZOOM = 18f
    }

    // 1. Checkeo de permisos, si hay buscar usuario

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        super.onViewCreated(view, savedInstanceState)
    }
    @SuppressLint("MissingPermission")
    override fun initializeView() {
        if (checkPermissions()) {
            // Obtener ubicación
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            // Localizando...
            searchingUser()
            adjustButtons()
        } else
            requestPermissions()
    }


    // 1. Configurar los botones
    private fun adjustButtons() {
        with(binding) {
            btMapButton.setOnClickListener {
                if (STATUS == ActivityStatus.READY || STATUS == ActivityStatus.PAUSE) {
                    sendCommandToService(Constants.ACTION_START_OR_RESUME_RUNNING)
                    updateUIRunning()
                } else if (STATUS == ActivityStatus.RUNNING) {
                    sendCommandToService(Constants.ACTION_PAUSE_RUNNING)
                    updateUIPause()
                }
            }
        }
    }

    // 2.* Localizando usuario
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            Log.d(TAG, "UI Location: ${p0.lastLocation?.accuracy}")
            if (p0.lastLocation?.accuracy!! <= 50){
                lastLocation = p0.lastLocation?.toLatLng()
                binding.mapView.getMapAsync(this@RunningFragment)
                fusedLocationProviderClient.removeLocationUpdates(this)
            }
        }
    }

    // 2.* Movimiento cámara
    val trackingCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            Log.d(TAG, "onLocationResult: TRACKING...")
            super.onLocationResult(p0)
            if (canUpdateCamera){
                lastLocation = p0.lastLocation?.toLatLng()
                Log.d(TAG, "AUTO move_camera")
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation!!,map.cameraPosition.zoom),300,null)
            }
        }
    }

    // 2.* Animación buscando usuario
    private fun searchingUser() {
        Log.d(TAG, "searchingUser: LOCALIZANDO...")
        timer?.cancel()
        // Timer para poner 3 puntitos *importante* al buscar gps
        timer = startScheduleTimerOnMain {
            if (binding.tvLocating.text.contains("..."))
                binding.tvLocating.text = getString(com.gf.common.R.string.obtaining_gps)
            else
                binding.tvLocating.text = binding.tvLocating.text.toString() + "."
        }
    }

    // 2.* Mapa y usuario, localizado, se configura el mapa y se llama a servicio
    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "onMapReady: MAPA CARGADO")
        map = p0
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.uiSettings.isMyLocationButtonEnabled = false

        // LOCALIZACIÓN ENCONTRADA
        map.isMyLocationEnabled = true



        with(binding) {
            tvLocating.text = getString(com.gf.common.R.string.obtaining_gps_done)
            lyLocating.setBackgroundResource(com.gf.common.R.color.green)
            btMapButton.visible()
            btMylocation.visible()
            pbLocating.invisible()
        }
        timer?.cancel()
        timer = startTimerOnMain(2000) { binding.lyLocating.collapse() }

        STATUS = ActivityStatus.READY

        // Default GOOGLE Location Button -> Invisible
        // val locationButton = hideGoogleLocationButton()

        map.setOnCameraMoveStartedListener {
            CAM_MOVING = true
            if (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                binding.btMylocation.isChecked = false
                CAM_TRACK = false
            }
        }

        map.setOnCameraIdleListener {
            CAM_MOVING = false
        }
        binding.btMylocation.isChecked = true

        binding.btMylocation.addOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                lastLocation?.let {
                    Log.d(TAG, "MANUAL move_camera")
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            it,
                            if (map.cameraPosition.zoom <= MIN_CAM_ZOOM)
                                CAM_ZOOM
                            else
                                map.cameraPosition.zoom

                        )
                    )
                }
                CAM_TRACK = true
            }
            else{
                CAM_TRACK = false
            }
        }

        lastLocation?.let {
            Log.d(TAG, "INITIAL move_camera")
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    it,
                    CAM_ZOOM
                )
            )
        }


        // Actualizar cámara
        fusedLocationProviderClient.requestLocationUpdates(
            trackingRequest,
            trackingCallback,
            Looper.getMainLooper()
        )

        collectFlowOnce(RunningService.uiState, ::updateUI, ::createUI)
    }



    // 4. Si es la primera vez que se inicia la carrera
    private fun createUI() {
        Log.d(TAG, "createUI: Primer inicio")
        recoveredState = false
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    // 4. Si ya se había iniciado la carrera
    private fun updateUI(runningUIState: RunningUIState) {
        // Se recupera una carrera
        if (recoveredState){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "updateUI: Recupero ${runningUIState.status}")
            when (runningUIState.status) {
                ActivityStatus.RUNNING -> updateUIRunning()
                ActivityStatus.PAUSE -> updateUIPause()
                else -> {}
            }
            recoveredState = false
        }
        // Pintado normal
        else{

            if (STATUS != runningUIState.status){
                Log.d(TAG, "updateUI: Estados distintos Vista -> ${STATUS} Servicio -> ${runningUIState.status}")
                when (runningUIState.status) {
                    ActivityStatus.RUNNING -> updateUIRunning()
                    ActivityStatus.PAUSE -> updateUIPause()
                    else -> {}
                }
            }
        }
        updateUIPolyline(runningUIState.points)
        updateUIStats(
            runningUIState.time,
            runningUIState.distance,
            runningUIState.speedLastKm
        )
    }

    // 4.* Actualizar lineas del UI
    private fun updateUIPolyline(points:List<List<LatLng>>){

        if (uiPoints == points.flatten())
            return

        Log.d(TAG, "updateUIPolyline: Actualizo linea con ${points.flatten().size} puntos")

        map.clear()

        points.forEach {
            val polylineOptions = PolylineOptions().apply {
                color(requireContext().getColor(com.gf.common.R.color.orange_quaternary)) // Color de la línea
                visible(true)
                zIndex(7f)
                width(8f) // Grosor de la línea en píxeles
            }
            it.forEach {
                polylineOptions.add(it)
            }

            polylineOptions.points
            map.addPolyline(polylineOptions)
        }
        uiPoints = points.flatten()
    }

    // 4.* Actualizar elementos interfaz
    private fun updateUIStats(time:Int, distance:Int,speed:Float){
        with(binding){
            if (distance < 0 || time < 0)
                return

            tvPanelDistance.text = (distance/1000f).format(2)
            tvPanelTime.text = StatCounter.formatTime(time)

            if (distance >= 50){
                // Minutos por kilómetro
                tvPanelSpeed.setSpeed(speed)
            }

        }
    }


    // 4.* Actualizar UI para estado RUNNING
    private fun updateUIRunning(){
        Log.d(TAG, "updateUIRunning: UI Corriendo")
        with(binding){
            btFinish.invisible()
            lyInfoPanel.expand()

            btMapButton.apply {
                text = getString(com.gf.common.R.string.btmap_detener)
                setTextColor(resources.getColor(com.gf.common.R.color.white))
                backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.purple_secondary))
            }
            btFinish.invisible()


            STATUS = ActivityStatus.RUNNING
            btMylocation.isChecked = true
        }
    }

    // 4.* Actualizar UI para estado PAUSE
    private fun updateUIPause(){
        Log.d(TAG, "updateUIPause: UI Pausada")
        with(binding){
            btFinish.visible()
            lyInfoPanel.visible()
            btMapButton.apply {
                text = getString(com.gf.common.R.string.btmap_reanudar)
                setTextColor(resources.getColor(com.gf.common.R.color.purple_secondary))
                backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.white))
            }

            STATUS = ActivityStatus.PAUSE
        }
    }

    private fun TextView.setSpeed(speed: Float){
        text = if (speed > 0) {
            val speedMinsPerKilometer = ((1 / speed) / 60) * 1000

            // Calcula los minutos y segundos
            val minutos = (speedMinsPerKilometer).toInt()
            val segundos = ((speedMinsPerKilometer * 60) % 60).toInt()

            // Formatea la cadena en "mm:ss"
            String.format("%02d:%02d", minutos, segundos)
        }
        else
            "0:00"
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value })
                initializeView()
            else
                handleNoGps()

        }

    @SuppressLint("InlinedApi")
    private fun checkPermissions(): Boolean {

        permissionToAsk = mutableSetOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= 33)
            permissionToAsk.add(Manifest.permission.POST_NOTIFICATIONS)


        return isPermissionGranted(
            setOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )

    }

    private fun requestPermissions() =
        requestMultiplePermissions.launch(
            permissionToAsk.toTypedArray()
        )

    private fun handleNoGps() {
        toast(com.gf.common.R.string.error_no_location)
        onBackPressed()
    }

    override fun onDestroyView() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroyView()
    }



    override fun onStart() {
        binding.mapView.onStart()
        super.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        binding.mapView.onResume()
        super.onResume()
    }

    override fun onLowMemory() {
        binding.mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    private fun sendCommandToService(action: String) =
        (requireActivity() as MainActivity).sendCommandToService(action)
}
