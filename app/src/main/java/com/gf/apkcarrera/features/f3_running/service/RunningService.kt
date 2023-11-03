package com.gf.apkcarrera.features.f3_running.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.cotesa.common.extensions.distanceTo
import com.gf.apkcarrera.MainActivity
import com.gf.common.entity.ActivityStatus
import com.gf.common.entity.RunningUIState
import com.gf.common.extensions.toLatLng
import com.gf.common.utils.Constants.ACTION_PAUSE_RUNNING
import com.gf.common.utils.Constants.ACTION_SHOW_RUNNING_FRAGMENT
import com.gf.common.utils.Constants.ACTION_START_OR_RESUME_RUNNING
import com.gf.common.utils.Constants.ACTION_STOP_RUNNING
import com.gf.common.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.gf.common.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.gf.common.utils.Constants.NOTIFICATION_ID
import com.gf.common.utils.StatCounter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class RunningService : LifecycleService() {

    // VAR Servicio
    private val TAG = "ServiceRunning"
    var isUnresumed = true

    // VAR Localización
    private val fusedLocationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(this)

    private val locationRequest: LocationRequest
        get() = LocationRequest.Builder( Priority.PRIORITY_HIGH_ACCURACY,1000).build()

    var lastLocation : Location? = null
    val pointsLastPoint : LatLng?
        get() = points.flatten().lastOrNull()

    private val distanceLastPoint : Double
        get () =
            if (pointsLastPoint != null && lastLocation != null)
                pointsLastPoint!!.distanceTo(lastLocation!!.toLatLng())
            else
                0.0


    // VAR Timer Carrera
    lateinit var timer: Timer
    var clockTime = 0

    // VAR AutoPause
    private var timeWithoutPoints : Int = 0
    var lastRegisterTime = 0

    var STATUS = ActivityStatus.RUNNING
    var points : MutableList<MutableList<LatLng>> = mutableListOf(mutableListOf())
    var pausedPoints = 0
    val statCounter = StatCounter()

    companion object{

        private val _uiState = MutableStateFlow<RunningUIState?>(null)
        val uiState get() = _uiState.asStateFlow()

        private const val PAUSE_MIN_DISTANCE = 1
        private const val RESUME_MIN_DISTANCE = 30
        private const val PAUSE_MIN_TIME = 30

        private const val SPEED_STAT_MIN_TIME = 60
        private const val SPEED_STAT_MIN_DISTANCE = 300
    }

    // 1. Comandos para controlar el servicio
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_RUNNING->{
                    STATUS = ActivityStatus.RUNNING
                    if (isUnresumed){
                        Log.d(TAG, "onStartCommand: START_RUNNING")
                        startForegroundService()
                        startTimer()
                        isUnresumed = false

                    }
                    else{
                        Log.d(TAG, "onStartCommand: RESUME_RUNNING")
                    }
                }
                ACTION_PAUSE_RUNNING->{
                    pauseRunning()
                    Log.d(TAG, "onStartCommand: PAUSE_RUNNING")
                }
                ACTION_STOP_RUNNING ->{
                    STATUS = ActivityStatus.DONE
                    stopTimer()
                    stopService()
                    Log.d(TAG, "onStartCommand: STOP_RUNNING")
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // 1. Inicio del servicio
    @SuppressLint("MissingPermission")
    private fun startForegroundService(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(com.gf.common.R.drawable.icon_run)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, null)
    }

    // 2. Recogida de datos
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            // Si la localización es null, no coger punto
            lastLocation = p0.lastLocation ?: return

            when (STATUS){
                ActivityStatus.RUNNING ->
                    action1AddPoint(p0.lastLocation!!)
                ActivityStatus.PAUSE ->
                    action2Paused(p0.lastLocation!!)
                else ->{}
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun action1AddPoint(location: Location) {
        val locLatLng = LatLng(location.latitude, location.longitude)

        // Si detecta a más de 30 metros de precisión, no coger
        if (location.accuracy > 30)
            return

        var segmentDistance = 10.0

        if (points.last().isNotEmpty())
            segmentDistance = points.flatten().last().distanceTo(locLatLng)


        if (segmentDistance >= 10) {
            pausedPoints = 0
            points.last().add(locLatLng)
            statCounter.add(segmentDistance.toInt(),clockTime-lastRegisterTime)
            lastRegisterTime = clockTime

        }
    }

    private fun action2Paused(location: Location){

        val curLatLng = LatLng(location.latitude,location.longitude)
        val distance = try{
            curLatLng.distanceTo(points.flatten().last())
        }catch (ex:Exception){
            0.0
        }


    }

    private fun pauseRunning(){
        points.add(mutableListOf())
        STATUS = ActivityStatus.PAUSE
    }

    private fun startTimer(){
        timer = Timer().apply {
            schedule(timerTask {
                if (STATUS == ActivityStatus.RUNNING){

                    if (lastLocation == null || pointsLastPoint == null || distanceLastPoint <= PAUSE_MIN_DISTANCE) {
                        timeWithoutPoints++
                        Log.d(TAG, "Time between points [$timeWithoutPoints / $PAUSE_MIN_TIME]")
                    }
                    else{
                        timeWithoutPoints = 0
                        Log.d(TAG, "Time between points RESET")
                    }


                    if (timeWithoutPoints > PAUSE_MIN_TIME){
                        pauseRunning()
                        timeWithoutPoints = 0
                    }
                }
                else if (STATUS == ActivityStatus.PAUSE){
                    Log.d(TAG, "action2Paused: Pausado ($distanceLastPoint -> Unpause in $RESUME_MIN_DISTANCE)")
                    if (pointsLastPoint!!.distanceTo(lastLocation!!.toLatLng()) >= RESUME_MIN_DISTANCE){
                        Log.d(TAG, "action2Paused: Se recupera la carrera")
                        STATUS = ActivityStatus.RUNNING
                    }
                }

                clockTime++
                updateUi(
                    RunningUIState(
                        time = clockTime,
                        distance = statCounter.totalDistance,
                        speedLastKm = statCounter.lastSpeed,
                        points = points,
                        status = STATUS
                    )
                )
                // Si se recorren 15 metros desde el punto de pausa se reanuda la actividad


            },0,1000)
        }
    }

    private fun stopTimer(){
        timer.cancel()
    }

    private fun stopService(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        stopSelf()
    }


    // Emitir datos
    private fun updateUi(uiState : RunningUIState) {
        lifecycleScope.launch {
            _uiState.emit(uiState)
        }
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this,MainActivity::class.java).also{
            it.action = ACTION_SHOW_RUNNING_FRAGMENT
        },
        FLAG_MUTABLE
    )

    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }
}