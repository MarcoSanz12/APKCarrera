package com.gf.apkcarrera.features.f3_running.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg03RunningBinding
import com.gf.apkcarrera.features.f3_running.service.ServiceRunning
import com.gf.common.entity.RunningUIState
import com.gf.common.extensions.collectFlowOnce
import com.gf.common.extensions.invisible
import com.gf.common.extensions.isPermissionGranted
import com.gf.common.extensions.toast
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseFragment
import com.gf.common.utils.Constants.ACTION_START_OR_RESUME_RUNNING
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class FragmentRunning : OnMapReadyCallback,BaseFragment<Frg03RunningBinding>() {

    private val TAG = "FragmentRunning"

    // VAR Map
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private var userLocation : Location? = null
    private var CAM_MOVING = false
    private var CAM_TRACK = false
    private lateinit var permissionToAsk: MutableSet<String>

    // VAR Actividad
    private var timer : Timer? = null

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
            assignButtons()
        }
        else
            requestPermissions()

    }
    // 2. Asignación de botones
    private fun assignButtons() {
        with(binding) {
            btMapButton.setOnClickListener {
                sendCommandToService(ACTION_START_OR_RESUME_RUNNING)
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
            if (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
                binding.btMylocation.isChecked = false
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

    private fun searchingUser(){
        Log.d(TAG, "searchingUser: LOCALIZANDO...")
        timer?.cancel()
        // Timer para poner 3 puntitos *importante* al buscar gps
        timer = startTimerOnMain {
            if (binding.tvLocating.text.contains("..."))
                binding.tvLocating.text = getString(com.gf.common.R.string.obtaining_gps)
            else
                binding.tvLocating.text = binding.tvLocating.text.toString() + "."
        }


    }

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
        timer = startTimerOnMain(2000,0) {  }

            Timer().apply{
            schedule(timerTask {
                MAIN.launch { binding.lyLocating.collapse() }

            },2000)
        }
    }

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
            if (p0.lastLocation?.accuracy!! <= 30){
                userLocation = p0.lastLocation
                userLocated()
            }

        }
    }

    // 3. Usuario localizado y mapa cargado, se observa la recolección del servicio
    private fun userLocated(){
        if (userLocation != null && this::map.isInitialized)
            foundUser()
        collectFlowOnce(ServiceRunning.uiState, ::updateUI, ::createUI)
    }

    /**
     * Si es la primera vez que se inicia
     */
    private fun createUI() {
        TODO("Not yet implemented")
    }

    /**
     * Si ya ha iniciado la carrera
     */
    private fun updateUI(runningUIState: RunningUIState) {

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
