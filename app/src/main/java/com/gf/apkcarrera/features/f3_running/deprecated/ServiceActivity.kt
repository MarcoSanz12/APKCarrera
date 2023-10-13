package com.gf.apkcarrera.features.f3_running.deprecated

/*
@AndroidEntryPoint
@Deprecated("Obsolete due to a new class", ReplaceWith("ServiceRunning","lol"),DeprecationLevel.HIDDEN)
class ServiceActivity : Service() {
    companion object{
        private const val MAX_PAUSED_POINTS = 5
        private const val PAUSE_MIN_DISTANCE = 30

        private const val SPEED_STAT_MIN_TIME = 60
        private const val SPEED_STAT_MIN_DISTANCE = 300
    }

    private val binder = RunningBinder()
    private lateinit var mNotification : Notification
    private var STATUS = ActivityStatus.RUNNING

    @Inject
    lateinit var activityViewModel: ActivityViewModel
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject
    lateinit var locationRequest: LocationRequest

    private val positionUpdater : PositionUpdater by lazy { PositionUpdater() }

    var contador = 0
    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    inner class RunningBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        //fun getService(): ServiceActivity = this@ServiceActivity
    }

    fun start(){
        mNotification = NotificationCompat.Builder(this,"running_channel")
            .setSmallIcon(com.gf.common.R.drawable.face_default)
            .setContentTitle("Running")
            .setContentInfo(contador.toString())
            .build()

        startForeground(1, mNotification)

        CoroutineScope(Dispatchers.Default).launch {
            activityViewModel.serviceStateFlow.collect{
                Log.d("ACTUALIZACION","Servicio : ${it}")
                STATUS = it
                if (STATUS == ActivityStatus.RUNNING)
                    positionUpdater.startTimer()
                if (STATUS == ActivityStatus.PAUSE || STATUS == ActivityStatus.DONE)
                    positionUpdater.stopTimer()

            }
        }

        initializeRegister()
    }

    private fun initializeRegister() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("ACTUALIZACION","Empiezo a recolectar")
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,positionUpdater,null)
        }

    }

    private inner class PositionUpdater : LocationCallback(){

        var oldLocation : Location? = null
        var lastLocation : Location? = null

        var clockTime = 0
        var lastRegisterTime = 0


        lateinit var timer: Timer
        var points : MutableList<MutableList<LatLng>> = mutableListOf(mutableListOf())
        var pausedPoints = 0
        val statCounter = StatCounter()
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            // Si la localización es null, no coger punto
            lastLocation = locationResult.lastLocation ?: return

            Log.d("ACTUALIZACION","Punto : X ${lastLocation?.latitude ?: 0} Y ${lastLocation?.longitude ?: 0}")


            when (STATUS){
                ActivityStatus.LOCATING ->
                    action0LocateGps(lastLocation!!)

                ActivityStatus.RUNNING ->
                    action1AddPoint(lastLocation!!)
                ActivityStatus.PAUSE ->{
                    action2Paused(lastLocation!!)
                }
                else ->{

                }
            }

            activityViewModel.updateUi(ActivityUIState(
                time = clockTime,
                distance = statCounter.totalDistance,
                speedLastKm = statCounter.lastSpeed,
                points = points,
                status = STATUS

            ))
        }


        @SuppressLint("MissingPermission")
        private fun action0LocateGps(location: Location){
            if (location.accuracy <= 30)
                STATUS = ActivityStatus.READY
        }

        @SuppressLint("MissingPermission")
        private fun action1AddPoint(location: Location) {
            val locLatLng = LatLng(location.latitude, location.longitude)

            // Si detecta a más de 30 metros de precisión, no coger
            if (location.accuracy > 30)
                return

            var segmentDistance = 10.0

            if (points.last().isNotEmpty())
                segmentDistance = points.last().last().distanceTo(locLatLng)


            if (segmentDistance >= 10) {
                pausedPoints = 0
                points.last().add(locLatLng)
                statCounter.add(segmentDistance.toInt(),clockTime-lastRegisterTime)
                lastRegisterTime = clockTime


            }
            // Si vas a menos de 0.5 m/s durante 5 segundos seguidos se considera pausa
            else if (segmentDistance < 0.5){
                pausedPoints ++
                if (pausedPoints > MAX_PAUSED_POINTS){
                    STATUS = ActivityStatus.PAUSE
                }
            }
        }

        fun startTimer(){
            timer = Timer()
            timer.scheduleAtFixedRate(timerTask {
                try {
                    if (STATUS == ActivityStatus.RUNNING)
                        clockTime++

                }catch (ex:Exception){
                    ex.printStackTrace()
                    timer.cancel()
                }

            },0,1000)

        }
        fun stopTimer(){
            timer.cancel()
        }

        private fun action2Paused(location: Location){
            val curLatLng = LatLng(location.latitude,location.longitude)
            val distance = try{
                curLatLng.distanceTo(points.last().last())
            }catch (ex:Exception){
                0.0
            }

            // Si se recorren 15 metros desde el punto de pausa se reanuda la actividad
            if (distance > PAUSE_MIN_DISTANCE)
                STATUS = ActivityStatus.RUNNING
        }

    }

    fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

}
*/
