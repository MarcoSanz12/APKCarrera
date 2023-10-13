package com.gf.apkcarrera.features.f3_running.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.gf.apkcarrera.MainActivity
import com.gf.common.entity.RunningUIState
import com.gf.common.utils.Constants.ACTION_PAUSE_RUNNING
import com.gf.common.utils.Constants.ACTION_SHOW_RUNNING_FRAGMENT
import com.gf.common.utils.Constants.ACTION_START_OR_RESUME_RUNNING
import com.gf.common.utils.Constants.ACTION_STOP_RUNNING
import com.gf.common.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.gf.common.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.gf.common.utils.Constants.NOTIFICATION_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServiceRunning : LifecycleService() {

    private val TAG = "ServiceRunning"

    var isUnresumed = true

    companion object{

        private val _uiState = MutableStateFlow<RunningUIState?>(null)
        val uiState get() = _uiState.asStateFlow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_RUNNING->{
                    if (isUnresumed){
                        Log.d(TAG, "onStartCommand: START_RUNNING")
                        startForegroundService()
                        isUnresumed = false

                    }
                    else{
                        Log.d(TAG, "onStartCommand: RESUME_RUNNING")
                    }


                }
                ACTION_PAUSE_RUNNING->{
                    Log.d(TAG, "onStartCommand: PAUSE_RUNNING")
                }
                ACTION_STOP_RUNNING ->{
                    stopSelf()
                    Log.d(TAG, "onStartCommand: STOP_RUNNING")
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

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
    }

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
        FLAG_UPDATE_CURRENT
    )

    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }
}