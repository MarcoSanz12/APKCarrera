package com.gf.apkcarrera

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.gf.apkcarrera.features.f3_activity.service.RunningService
import com.gf.common.extensions.getDestinationId
import com.gf.common.extensions.invisible
import com.gf.common.extensions.navigateToMenuItem
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override val navController by lazy { (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController }
    private val topLevelIds = setOf(R.id.fragmentSplash,R.id.fragmentInitial, R.id.fragmentFeed)
    var mService: RunningService? = null
    var mBound: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        screenSplash.setKeepOnScreenCondition{false}

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar : Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.apply {
            setupWithNavController(navController)
            setOnItemSelectedListener {
                navController.navigateToMenuItem(it)
            }
        }
        toolbar.setupWithNavController(navController, AppBarConfiguration(topLevelIds))

        navController.addOnDestinationChangedListener{ navController: NavController, navDestination: NavDestination, bundle: Bundle? ->

           if (topLevelIds.contains(navDestination.id))
                navController.graph.setStartDestination(navDestination.id)

            when (navDestination.getDestinationId()){
                R.id.fragmentFeed -> {
                    toolbar.visible()
                    bottomNavigationView.visible()
                }
                R.id.fragmentInitial->{
                    toolbar.invisible()
                    bottomNavigationView.invisible()
                }
            }
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = (service as RunningService.RunningBinder).getService()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
        }
    }

    fun startService() {
        bindService(
            Intent(
                this@MainActivity,
                RunningService::class.java
            ), mConnection, BIND_AUTO_CREATE
        )
        mBound = true
    }

    fun stopService() {
        if (mBound) {
            // Detach our existing connection.
            unbindService(mConnection)
            mBound = false
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - RESUME")
    }
    override fun onDestroy() {
        super.onDestroy()
        stopService()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - DESTROY")
    }


    override fun onStop() {
        super.onStop()
        Log.d("STATUS_LIFE", "${this.javaClass.simpleName} - STOP")
    }
}