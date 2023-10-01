package com.gf.apkcarrera.features.f3_activity.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.cotesa.common.extensions.distanceTo
import com.cotesa.common.extensions.notNull
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg03ActivityBinding
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.extensions.invisible
import com.gf.common.extensions.isPermissionGranted
import com.gf.common.extensions.visible
import com.gf.common.functional.AccuracyInterpolator
import com.gf.common.functional.LatLngInterpolator
import com.gf.common.platform.BaseFragment
import com.gf.common.utils.StatCounter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.math.abs
import kotlin.math.sqrt


class FragmentActivity : BaseFragment<Frg03ActivityBinding>(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var positionUpdater: PositionUpdater
    private val points : MutableList<LatLng> = mutableListOf()
    private var STATUS = STATUS_LOCATING
    private var USER_PAUSED = true
    private var CAM_MOVING = false
    private var CAM_TRACK = true
    private lateinit var timer : Timer
    private val handler by lazy{ Handler()}
    private var runnable : Runnable? = null
    private var TAG = "lifecycle"

    private lateinit var permissionToAsk : MutableSet<String>

    lateinit var activityModel : ActivityModel

    // Cola usada para registrar las distancias y tiempos del último kilómetro
    private val statCounter = StatCounter()

    private var clockTime = 0
    private var lastRegisterTime = 0

    companion object{
        private const val STATUS_LOCATING = 0
        private const val STATUS_READY = 1
        private const val STATUS_RUNNING = 2
        private const val STATUS_PAUSE = 3
        private const val STATUS_DONE = 4
        private const val CAM_ZOOM = 18f
        private const val MAX_PAUSED_POINTS = 5
        private const val PAUSE_MIN_DISTANCE = 30

        private const val SPEED_STAT_MIN_TIME = 60
        private const val SPEED_STAT_MIN_DISTANCE = 300
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityModel = ActivityModel()
        initializeView()
        Log.d("VIDA","${javaClass.simpleName} OnCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("VIDA","${javaClass.simpleName} OnCreateView")
        if (!(requireActivity() as MainActivity).mBound)
            (requireActivity() as MainActivity).startService()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("VIDA","${javaClass.simpleName} OnDetach")

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("VIDA","${javaClass.simpleName} OnDestroy")

    }

    private fun initializeView(){
        if (checkPermissions())
            createMap()
        else
            requestPermissions()
    }

    override fun onStop() {
        super.onStop()
        Log.d("VIDA","${javaClass.simpleName} OnStop")
    }

    override fun onResume() {
        super.onResume()
        Log.d("VIDA","${javaClass.simpleName} OnResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("VIDA","${javaClass.simpleName} OnDestroyView")
    }

    private fun createMap(){
        mapFragment = SupportMapFragment.newInstance()
        childFragmentManager
            .beginTransaction()
            .add(R.id.ly_map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
        (requireActivity() as MainActivity).startService()
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value })
                createMap()
            else
                handleNoGps()

        }
    private fun requestPermissions() =
        requestMultiplePermissions.launch(
            permissionToAsk.toTypedArray()
        )

    private fun handleNoGps(){
        Toast.makeText(requireContext(), getString(com.gf.common.R.string.error_no_location), Toast.LENGTH_SHORT).show()
        onBackPressed()
    }

    @SuppressLint("InlinedApi")
    private fun checkPermissions() : Boolean{

        permissionToAsk = mutableSetOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= 33)
            permissionToAsk.add(Manifest.permission.POST_NOTIFICATIONS)


        return isPermissionGranted(setOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        ))
    }

    private inner class PositionUpdater : LocationSource,LocationCallback(){
        var locationChangeListener: LocationSource.OnLocationChangedListener? = null
        var oldLocation : Location? = null
        var lastLocation : Location? = null
        var pausedPoints = 0

        init {
            binding.btMapButton.apply {
                visible()
                setOnClickListener {
                    when(STATUS){
                        STATUS_READY -> startActivity()
                        STATUS_RUNNING -> pauseActivity(true)
                        STATUS_PAUSE -> resumeActivity()
                    }
                }
            }
        }

        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            // Si la localización es null, no coger punto
            lastLocation = locationResult.lastLocation ?: return

            if (oldLocation == null){
                oldLocation = lastLocation
                locationChangeListener?.onLocationChanged(lastLocation!!)
            }
            else {
                animateMovement()
            }

            if (!isAdded)
                return

            when (STATUS){
                STATUS_LOCATING ->
                    action0LocateGps(lastLocation!!)

                STATUS_RUNNING ->
                    action1AddPoint(lastLocation!!)
                STATUS_PAUSE ->{
                    action2Paused(lastLocation!!)
                }
                STATUS_DONE ->{

                }
            }
        }

        /**
         * Se encarga de interpolar el circulo del usuario, para moverlo de forma fluida además de
         * mover la cámara segun este se vaya moviendo
         */
        private fun animateMovement() {
            runnable.notNull {
                handler.removeCallbacks(it)
            }

            val startPosition = LatLng(oldLocation!!.latitude, oldLocation!!.longitude)
            val startAccuracy = oldLocation!!.accuracy
            val startBearing = oldLocation!!.bearing
            val start = SystemClock.uptimeMillis()
            val interpolator = LinearInterpolator()
            val accInterpolator = AccelerateDecelerateInterpolator()
            val durationInMs = 1000F
            val latLngInterpolator = LatLngInterpolator.Linear()
            val floatInterpolator = AccuracyInterpolator.Linear()

            runnable = object : Runnable {

                var elapsed: Long = 0
                var t: Float = 0.0f
                var v: Float = 0.0f
                var vAcc: Float = 0.0f

                override fun run() {
                    elapsed = SystemClock.uptimeMillis() - start;
                    t = (elapsed.toFloat() / durationInMs)
                    v = interpolator.getInterpolation(t)
                    vAcc = accInterpolator.getInterpolation(t)

                    val position = latLngInterpolator.interpolate(
                        v,
                        startPosition,
                        LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
                    )
                    val interAccuracy =
                        floatInterpolator.interpolate(vAcc, startAccuracy, lastLocation!!.accuracy)
                    val interBearing =
                        floatInterpolator.interpolate(vAcc, startBearing, lastLocation!!.bearing)

                    val interLocation = Location("")
                    interLocation.apply {
                        latitude = position.latitude
                        longitude = position.longitude
                        accuracy = interAccuracy
                        bearing = interBearing
                    }

                    if (!CAM_MOVING && CAM_TRACK){

                        if (map.cameraPosition.zoom < CAM_ZOOM){
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position,CAM_ZOOM))
                        }
                        else
                            map.animateCamera(CameraUpdateFactory.newLatLng(position))

                        CAM_MOVING = true

                        Log.d("ANIMATUR","Animaturu!")
                    }

                    oldLocation = interLocation

                    locationChangeListener?.onLocationChanged(interLocation)

                    // Repeat till progress is complete.
                    if (t < 1) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 8);
                    }
                }
            }

            handler.post(runnable!!)
        }

        override fun activate(p0: LocationSource.OnLocationChangedListener) {
            locationChangeListener = p0
        }

        override fun deactivate() {
            locationChangeListener = null
        }

        @SuppressLint("MissingPermission")
        private fun action0LocateGps(location: Location){
            if (location.accuracy > 30)
                return

            // LOCALIZACIÓN ENCONTRADA
            map.isMyLocationEnabled = true



            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude,location.longitude),
                CAM_ZOOM
            ))

            STATUS = STATUS_READY
            timer.cancel()
            binding.tvLocating.text = getString(com.gf.common.R.string.obtaining_gps_done)
            binding.lyLocating.setBackgroundResource(com.gf.common.R.color.green)
            binding.pbLocating.invisible()

            timer = Timer().apply{
                schedule(timerTask {
                    MAIN.launch { binding.lyLocating.collapse() }

                },2000)
            }
        }

        @SuppressLint("MissingPermission")
        private fun action1AddPoint(location: Location) {
            val locLatLng = LatLng(location.latitude, location.longitude)

            // Si detecta a más de 30 metros de precisión, no coger
            if (location.accuracy > 30)
                return

            var segmentDistance = 10.0

            if (points.isNotEmpty())
                segmentDistance = points.last().distanceTo(locLatLng)

            Log.d("DISTANCE",segmentDistance.toString())

            if (segmentDistance >= 10) {
                pausedPoints = 0
                points.add(locLatLng)
                updatePolyline(map)
                statCounter.add(segmentDistance.toInt(),clockTime-lastRegisterTime)
                lastRegisterTime = clockTime
                binding.apply {
                    tvPanelDistance.text = statCounter.distanceKm()
                    // Mostramos distancia unicamente cuando haya unas estadisticas en condiciones
                    if (statCounter.totalDistance >= SPEED_STAT_MIN_DISTANCE && statCounter.getTime() >= SPEED_STAT_MIN_TIME)
                        statCounter.speedMinKm().notNull {
                            tvPanelSpeed.text = it
                        }
                }
                Log.d("GPS_Update", "Points: ${points.size}")
            }
            // Si vas a menos de 0.5 m/s durante 5 segundos seguidos se considera pausa
            else if (segmentDistance < 0.5){
                pausedPoints ++
                if (pausedPoints > MAX_PAUSED_POINTS){
                    pauseActivity(false)
                }
            }
        }

        private fun action2Paused(location: Location){
            if (!USER_PAUSED){
                val curLatLng = LatLng(location.latitude,location.longitude)
                val distance = try{
                    curLatLng.distanceTo(activityModel.points.last().last())
                }catch (ex:Exception){
                    0.0
                }

                // Si se recorren 15 metros desde el punto de pausa se reanuda la actividad
                if (distance > PAUSE_MIN_DISTANCE){
                    resumeActivity()
                }
            }
        }

        private fun startActivity(){
            binding.lyInfoPanel.expand()

            (activity as? MainActivity)?.mService?.start()

            binding.btMapButton.apply {
                text = getString(com.gf.common.R.string.btmap_detener)
                setTextColor(resources.getColor(com.gf.common.R.color.white))
                backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.purple_secondary))
            }

            STATUS = STATUS_RUNNING
            binding.btMylocation.isChecked = true
            startTimer()
        }

        private fun pauseActivity(userPause : Boolean){
            USER_PAUSED = userPause
            pausedPoints = 0
            binding.btFinish.visible()
            activityModel.points.add(points.toList())
            points.clear()
            binding.btMapButton.apply {
                text = getString(com.gf.common.R.string.btmap_reanudar)
                setTextColor(resources.getColor(com.gf.common.R.color.purple_secondary))
                backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.white))
            }

            STATUS = STATUS_PAUSE
            stopTimer()
        }

        private fun resumeActivity(){
            binding.btMapButton.apply {
                text = getString(com.gf.common.R.string.btmap_detener)
                setTextColor(resources.getColor(com.gf.common.R.color.white))
                backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.purple_secondary))
            }
            STATUS = STATUS_RUNNING
            startTimer()
            binding.btFinish.invisible()
        }
    }

    private fun updatePolyline(map:GoogleMap){
        map.clear()

        val paintPoints = activityModel.points.toMutableList()
        paintPoints.add(points)
        paintPoints.forEach {
            val polylineOptions = PolylineOptions().apply {
                color(requireContext().getColor(com.gf.common.R.color.orange_quaternary)) // Color de la línea
                visible(true)
                zIndex(7f)
                width(8f) // Grosor de la línea en píxeles
            }
            it.forEach {
                polylineOptions.add(it)
            }
            map.addPolyline(polylineOptions)
        }

    }
    fun simplifyDouglasPeucker(points: List<LatLng>, epsilon: Double): List<LatLng> {
        if (points.size < 3) {
            return points
        }

        val firstPoint = points.first()
        val lastPoint = points.last()

        var maxDistance = 0.0
        var index = 0

        for (i in 1 until points.size - 1) {
            val distance = perpendicularDistance(points[i], firstPoint, lastPoint)
            if (distance > maxDistance) {
                maxDistance = distance
                index = i
            }
        }

        return if (maxDistance > epsilon) {
            val simplified1 = simplifyDouglasPeucker(points.subList(0, index + 1), epsilon)
            val simplified2 = simplifyDouglasPeucker(points.subList(index, points.size), epsilon)
            simplified1.dropLast(1) + simplified2
        } else {
            listOf(firstPoint, lastPoint)
        }
    }

    private fun perpendicularDistance(p: LatLng, p1: LatLng, p2: LatLng): Double {
        val area = abs(0.5 * (p1.longitude * p2.latitude + p2.longitude * p.latitude + p.latitude * p1.longitude - p2.longitude * p1.latitude - p.latitude * p2.longitude - p1.longitude * p.latitude))
        val bottom = sqrt(Math.pow(p1.longitude - p2.longitude, 2.0) + Math.pow(p1.latitude - p2.latitude, 2.0))
        return area / bottom
    }

    override fun onPause() {
        super.onPause()
        if (::positionUpdater.isInitialized){
            fusedLocationProviderClient.removeLocationUpdates(positionUpdater)
        }
    }

    private fun startTimer(){
        timer = Timer()
        timer.scheduleAtFixedRate(timerTask {
          try {
              if (STATUS == STATUS_RUNNING) {
                  clockTime++
                  val xd = (activity as? MainActivity)?.mService!!.contador++
                  MAIN.launch {
                      //binding.tvPanelTime.text = StatCounter.formatTime(clockTime)
                      binding.tvPanelTime.text = xd.toString()
                  }
              }
          }catch (ex:Exception){
              ex.printStackTrace()
              timer.cancel()
          }

        },0,1000)

    }
    private fun stopTimer(){
        timer.cancel()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.6315, -4.7220),9f))

        positionUpdater = PositionUpdater()
        map.setLocationSource(positionUpdater)
        map.isMyLocationEnabled = true

        val locationButton = (binding.lyMapContainer.findViewById<View>(Integer.parseInt("1")).parent as View)
            .findViewById<ImageView>(Integer.parseInt("2"))

        locationButton.invisible()

        map.setOnCameraMoveStartedListener {
            CAM_MOVING = true
            if (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
                binding.btMylocation.isChecked = false
        }

        binding.btMylocation.addOnCheckedChangeListener { button, isChecked ->
            if (isChecked){
                locationButton.callOnClick()
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

        val locationRequest = LocationRequest.Builder( Priority.PRIORITY_HIGH_ACCURACY,1000).build()

        // Timer para poner 3 puntitos *importante* al buscar gps
        timer = Timer().apply {
            scheduleAtFixedRate(timerTask {
                MAIN.launch {
                    if (binding.tvLocating.text.contains("..."))
                        binding.tvLocating.text = getString(com.gf.common.R.string.obtaining_gps)
                    else
                        binding.tvLocating.text = binding.tvLocating.text.toString() + "."
                }

            }
                ,0,1000)
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, positionUpdater, null)
    }

}