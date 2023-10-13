
package com.gf.apkcarrera.features.f3_running.deprecated

/*
@AndroidEntryPoint
@Deprecated("Obsolete due to a new class", ReplaceWith("FragmentRunning","lol"),DeprecationLevel.HIDDEN)
class FragmentActivity : BaseFragment<Frg03RunningBinding>(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private var STATUS = ActivityStatus.LOCATING
    private var USER_PAUSED = true
    private var CAM_MOVING = false
    private var CAM_TRACK = true
    private lateinit var timer : Timer

    private lateinit var accurateLocationCallback : AccurateLocationCallback

    private lateinit var permissionToAsk : MutableSet<String>

    @Inject
    lateinit var activityViewModel : ActivityViewModel

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var locationRequest: LocationRequest



    companion object{
        private const val LOCATION_MIN_ACCURACY = 30
        private const val CAM_ZOOM = 18f
    }


    override fun initObservers() {
        with(activityViewModel){
            collectFlow(uiStateFlow,Lifecycle.State.STARTED,::updateUI)
        }
    }

    override fun initializeView(){
        setMapButton()
        if (checkPermissions())
            createMap()
        else
            requestPermissions()
    }

    private fun updateUI(it: ActivityUIState) {
        Log.d("ACTUALIZACION","Fragment : ${it}")

        if (STATUS != it.status){
            when (it.status){
                ActivityStatus.LOCATING ->{}
                ActivityStatus.RUNNING -> startActivity()
                ActivityStatus.PAUSE -> pauseActivity(false)
                ActivityStatus.DONE -> endActivity()
                else->{}
            }
        }

        with(binding){
            tvPanelDistance.text = String.format("%.2f",it.distance / 3600.0)
            tvPanelTime.text = StatCounter.formatTime(it.time)

            // Minutos por kilómetro
            tvPanelSpeed.setSpeed(it.speedLastKm)
        }

    }

    private fun createMap(){
        if (!this::mapFragment.isInitialized){
            mapFragment = SupportMapFragment.newInstance()
            childFragmentManager
                .beginTransaction()
                .add(R.id.ly_map_container, mapFragment)
                .commit()

            mapFragment.getMapAsync(this)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.6315, -4.7220),9f))
        map.isMyLocationEnabled = true

        // Default GOOGLE Location Button -> Invisible
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

        // Hasta que no encuentre una precisión de 30 metros no pasa a estar READY
        accurateLocationCallback = AccurateLocationCallback(LOCATION_MIN_ACCURACY,::readyActivity)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,accurateLocationCallback,null)
    }
    @SuppressLint("MissingPermission")
    private fun readyActivity(location: Location) {
        // LOCALIZACIÓN ENCONTRADA
        map.isMyLocationEnabled = true

        Log.d("PROCESO","Actividad Ready!")

        val location = LatLng(location.latitude,location.longitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                CAM_ZOOM
            ))
        timer.cancel()
        with(binding){
            tvLocating.text = getString(com.gf.common.R.string.obtaining_gps_done)
            lyLocating.setBackgroundResource(com.gf.common.R.color.green)
            btMapButton.visible()
            btMylocation.visible()
            pbLocating.invisible()
        }

        timer = Timer().apply{
            schedule(timerTask {
                MAIN.launch { binding.lyLocating.collapse() }

            },2000)
        }

        STATUS = ActivityStatus.READY

        if (this::accurateLocationCallback.isInitialized)
            fusedLocationProviderClient.removeLocationUpdates(accurateLocationCallback)
    }
    private fun startActivity(){
        binding.lyInfoPanel.expand()

        binding.btMapButton.apply {
            text = getString(com.gf.common.R.string.btmap_detener)
            setTextColor(resources.getColor(com.gf.common.R.color.white))
            backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.purple_secondary))
        }


        STATUS = ActivityStatus.RUNNING
        binding.btMylocation.isChecked = true
    }

    private fun pauseActivity(userPause : Boolean){
        USER_PAUSED = userPause
        binding.btFinish.visible()
        binding.btMapButton.apply {
            text = getString(com.gf.common.R.string.btmap_reanudar)
            setTextColor(resources.getColor(com.gf.common.R.color.purple_secondary))
            backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.white))
        }

        STATUS = ActivityStatus.PAUSE
        activityViewModel.updateServiceState(ActivityStatus.PAUSE)
    }

    private fun endActivity(){
        STATUS = ActivityStatus.DONE
        activityViewModel.updateServiceState(ActivityStatus.DONE)
    }


    private fun startRunningService() {
        if (!(requireActivity() as MainActivity).mBound)
            (requireActivity() as MainActivity).startService()
    }

    private fun TextView.setSpeed(speed: Int){
        val speedMinsKm = ((1.0 / speed) / 60.0)

        // Calcula los minutos y segundos
        val minutos = (speedMinsKm).toInt()
        val segundos = ((speedMinsKm * 60) % 60).toInt()

        // Formatea la cadena en "mm:ss"
        text = String.format("%02d:%02d", minutos, segundos)
    }
    private fun setMapButton(){
        binding.btMapButton.apply {
            visible()
            setOnClickListener {
                when(STATUS){
                    ActivityStatus.READY, ActivityStatus.PAUSE, ActivityStatus.DONE ->{
                        startActivity()
                        startRunningService()

                    }
                    ActivityStatus.RUNNING -> pauseActivity(true)
                    else ->{}
                }

                activityViewModel.updateServiceState(STATUS)
            }
        }
    }

    private fun updatePolyline(map:GoogleMap,points:List<List<LatLng>>){
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
}*/
