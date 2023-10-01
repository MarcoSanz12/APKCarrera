package com.gf.apkcarrera.features.f3_activity.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RunningService : Service() {

    private val binder = RunningBinder()
    private lateinit var mNotification : Notification

    var contador = 0
    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    inner class RunningBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): RunningService = this@RunningService
    }

    fun start(){
        //TODO: METER REGISTRO CARRERA EN EL SERVICE
        mNotification = NotificationCompat.Builder(this,"running_channel")
            .setSmallIcon(com.gf.common.R.drawable.face_default)
            .setContentTitle("Running")
            .setContentInfo(contador.toString())
            .build()

        startForeground(1, mNotification)
    }

    enum class ACTION{
        START,STOP
    }
}