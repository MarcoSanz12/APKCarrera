package com.gf.apkcarrera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.gf.apkcarrera.features.f3_running.service.ServiceRunning
import com.gf.common.extensions.getDestinationId
import com.gf.common.extensions.invisible
import com.gf.common.extensions.navigateToMenuItem
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseActivity
import com.gf.common.utils.Constants.ACTION_SHOW_RUNNING_FRAGMENT
import com.gf.common.utils.Constants.ACTION_STOP_RUNNING
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override val navController by lazy { (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController }
    private val topLevelIds = setOf(R.id.fragmentSplash,R.id.fragmentInitial, R.id.fragmentFeed)

    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        screenSplash.setKeepOnScreenCondition{false}

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToRunningFragmentIfNeeded(intent)

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToRunningFragmentIfNeeded(intent)
    }
    private fun navigateToRunningFragmentIfNeeded(intent:Intent?){
        if (intent?.action == ACTION_SHOW_RUNNING_FRAGMENT){
            navController.navigate(R.id.action_global_fragmentRunning)
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - RESUME")
    }
    override fun onDestroy() {
        super.onDestroy()
        sendCommandToService(ACTION_STOP_RUNNING)
        Log.d("STATUS_LIFE","${this.javaClass.simpleName} - DESTROY")
    }


    override fun onStop() {
        super.onStop()
        Log.d("STATUS_LIFE", "${this.javaClass.simpleName} - STOP")
    }

    fun sendCommandToService (action : String) =
        Intent(this, ServiceRunning::class.java).also {
            it.action = action
            this.startService(it)
        }
}