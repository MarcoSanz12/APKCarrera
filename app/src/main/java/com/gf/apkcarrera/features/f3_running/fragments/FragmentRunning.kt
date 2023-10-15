package com.gf.apkcarrera.features.f3_running.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg03RunningBinding
import com.gf.apkcarrera.features.f3_running.service.ServiceRunning
import com.gf.common.entity.ActivityStatus
import com.gf.common.entity.RunningUIState
import com.gf.common.extensions.collectFlowOnce
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject

@AndroidEntryPoint
class FragmentRunning : OnMapReadyCallback,BaseFragment<Frg03RunningBinding>() {

    private val TAG = "FragmentRunning"

    // VAR Map
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private var userLocation : Location? = null
    private var CAM_MOVING = false
    private var CAM_TRACK = true
    private lateinit var permissionToAsk: MutableSet<String>

    // VAR Actividad
    private var timer : Timer? = null
    private var recoveredState : Boolean = true
    private var STATUS : ActivityStatus = ActivityStatus.LOCATING
    private var uiPoints = listOf<LatLng>()

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var locationRequest: LocationRequest

    companion object{
        private const val LOCATION_MIN_ACCURACY = 30
        private const val CAM_ZOOM = 18f
    }



    // 1. Checkeo de permisos
    override fun initializeView() {
        if (checkPermissions()){
            createMap()
            adjustButtons()
        }
        else
            requestPermissions()

    }

    // 1. Configurar los botones
    private fun adjustButtons(){
        with(binding) {
            btMapButton.setOnClickListener {
                if (STATUS == ActivityStatus.READY || STATUS == ActivityStatus.PAUSE){
                    sendCommandToService(Constants.ACTION_START_OR_RESUME_RUNNING)
                    updateUIRunning()
                }
                else if (STATUS == ActivityStatus.RUNNING){
                    sendCommandToService(Constants.ACTION_PAUSE_RUNNING)
                    updateUIPause()
                }
            }
        }
    }

    // 2. Creación del mapa
    @SuppressLint("MissingPermission")
    private fun createMap() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        if (!this::mapFragment.isInitialized) {
            mapFragment = SupportMapFragment.newInstance()
            childFragmentManager
                .beginTransaction()
                .add(R.id.ly_map_container, mapFragment)
                .commit()

            mapFragment.getMapAsync(this)


        }


    }
    // 2.* Mapa cargado
    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "onMapReady: MAPA CARGADO")
        map = p0
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.6315, -4.7220),9f))
        map.isMyLocationEnabled = true

        // Default GOOGLE Location Button -> Invisible
        val locationButton = hideGoogleLocationButton()

        // Localizando...
        searchingUser()


        map.setOnCameraMoveStartedListener {
            CAM_MOVING = true
            if (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                binding.btMylocation.isChecked = false
                CAM_TRACK = false
            }

        }

        binding.btMylocation.addOnCheckedChangeListener { button, isChecked ->
            if (isChecked){
                locationButton?.callOnClick()
                CAM_TRACK = true
            }
            else{
                CAM_TRACK = false
            }
        }

        binding.btMylocation.isChecked = true


        map.setOnCameraIdleListener {
            CAM_MOVING = false
        }



    }

    // 2.* Animación buscando usuario
    private fun searchingUser(){
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
    // 2.* Animación usuario localizado
    @SuppressLint("MissingPermission")
    private fun foundUser(){
        // LOCALIZACIÓN ENCONTRADA
        map.isMyLocationEnabled = true

        Log.d(TAG,"searchingUser: LOCALIZADO!")

        val location = LatLng(userLocation!!.latitude,userLocation!!.longitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                CAM_ZOOM
            ))

        with(binding){
            tvLocating.text = getString(com.gf.common.R.string.obtaining_gps_done)
            lyLocating.setBackgroundResource(com.gf.common.R.color.green)
            btMapButton.visible()
            btMylocation.visible()
            pbLocating.invisible()
        }
        timer?.cancel()
        timer = startTimerOnMain(2000) {  binding.lyLocating.collapse() }

        STATUS = ActivityStatus.READY

    }

    // 2.* Esconder botón de google
    private fun hideGoogleLocationButton(): ImageView? {
        val locationButton =
            (binding.lyMapContainer.findViewById<View>(Integer.parseInt("1")).parent as View)
                .findViewById<ImageView>(Integer.parseInt("2"))
        locationButton.invisible()
        return locationButton
    }

    // 2.* Localizando usuario
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if (CAM_TRACK && userLocation != null)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(p0.lastLocation!!.toLatLng(),map.cameraPosition.zoom)
                )
            if (p0.lastLocation?.accuracy!! <= 30 && userLocation == null){
                userLocation = p0.lastLocation
                userLocated()
            }

        }
    }

    // 3. Usuario localizado y mapa cargado, se observa la recolección del servicio
    private fun userLocated(){
        if (userLocation == null || !this::map.isInitialized)
            return

        foundUser()
        collectFlowOnce(ServiceRunning.uiState, ::updateUI, ::createUI)
    }

    // 4. Si es la primera vez que se inicia la carrera
    private fun createUI() {
        Log.d(TAG, "createUI: Primer inicio")
        recoveredState = false
    }


    // 4. Si ya se había iniciado la carrera
    private fun updateUI(runningUIState: RunningUIState) {
        // Se recupera una carrera
        if (recoveredState){
            Log.d(TAG, "updateUI: Recupero ${runningUIState.status}")
            when (runningUIState.status) {
                ActivityStatus.RUNNING -> updateUIRunning()
                ActivityStatus.PAUSE -> updateUIPause()
                else -> {}
            }
            recoveredState = false
        }
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
    private fun updateUIStats(time:Int, distance:Int,speed:Int){
        with(binding){
            tvPanelDistance.text = String.format("%.2f",distance / 3600.0)
            tvPanelTime.text = StatCounter.formatTime(time)

            // Minutos por kilómetro
            tvPanelSpeed.setSpeed(speed)
        }
    }

    // 4.* Actualizar UI para estado RUNNING
    private fun updateUIRunning(){
        Log.d(TAG, "updateUIRunning: UI Corriendo")
        with(binding){
            lyInfoPanel.expand()

            btMapButton.apply {
                text = getString(com.gf.common.R.string.btmap_detener)
                setTextColor(resources.getColor(com.gf.common.R.color.white))
                backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.purple_secondary))
            }


            STATUS = ActivityStatus.RUNNING
            btMylocation.isChecked = true
        }
    }

    // 4.* Actualizar UI para estado PAUSE
    private fun updateUIPause(){
        Log.d(TAG, "updateUIPause: UI Pausada")
        with(binding){
            btFinish.visible()
            btMapButton.apply {
                text = getString(com.gf.common.R.string.btmap_reanudar)
                setTextColor(resources.getColor(com.gf.common.R.color.purple_secondary))
                backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.white))
            }

            STATUS = ActivityStatus.PAUSE
        }
    }

    private fun TextView.setSpeed(speed: Int){
        if (speed == 0)
            text = "00:00"
        else{
            val speedMinsKm = ((1.0 / speed) / 60.0)

            // Calcula los minutos y segundos
            val minutos = (speedMinsKm).toInt()
            val segundos = ((speedMinsKm * 60) % 60).toInt()

            // Formatea la cadena en "mm:ss"
            text = String.format("%02d:%02d", minutos, segundos)
        }

    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value })
                createMap()
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

    private fun sendCommandToService(action: String) =
        (requireActivity() as MainActivity).sendCommandToService(action)
}
