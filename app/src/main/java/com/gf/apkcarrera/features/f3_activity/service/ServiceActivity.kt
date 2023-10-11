package com.gf.apkcarrera.features.f3_activity.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cotesa.common.extensions.distanceTo
import com.gf.apkcarrera.features.f3_activity.viewmodel.ActivityViewModel
import com.gf.common.entity.ActivityStatus
import com.gf.common.utils.StatCounter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
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

    var contador = 0
    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    inner class RunningBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): ServiceActivity = this@ServiceActivity
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
                STATUS = it
            }
        }

        initializeRegister()
    }

    private fun initializeRegister() {

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
        }

        /**
         * Se encarga de interpolar el circulo del usuario, para moverlo de forma fluida además de
         * mover la cámara segun este se vaya moviendo
         */
        /*private fun animateMovement() {
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

                        if (map.cameraPosition.zoom < FragmentActivity.CAM_ZOOM){
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(position,
                                    FragmentActivity.CAM_ZOOM
                                ))
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
        }*/


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

            Log.d("DISTANCE",segmentDistance.toString())

            if (segmentDistance >= 10) {
                pausedPoints = 0
                points.last().add(locLatLng)
                statCounter.add(segmentDistance.toInt(),clockTime-lastRegisterTime)
                lastRegisterTime = clockTime

                Log.d("GPS_Update", "Points: ${points.size}")
            }
            // Si vas a menos de 0.5 m/s durante 5 segundos seguidos se considera pausa
            else if (segmentDistance < 0.5){
                pausedPoints ++
                if (pausedPoints > MAX_PAUSED_POINTS){
                    STATUS = ActivityStatus.PAUSE
                }
            }
        }

        private fun startTimer(){
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
        private fun stopTimer(){
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