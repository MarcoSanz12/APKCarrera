package com.gf.apkcarrera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.children
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gf.apkcarrera.databinding.ActivityMainBinding
import com.gf.apkcarrera.features.f3_running.service.ServiceRunning
import com.gf.common.extensions.invisible
import com.gf.common.extensions.navigateToMenuItem
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseActivity
import com.gf.common.utils.Constants.ACTION_SHOW_RUNNING_FRAGMENT
import com.gf.common.utils.Constants.ACTION_STOP_RUNNING
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override val navController by lazy { (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController }

    private val noActionBarIds = setOf(R.id.fragmentSplash,R.id.fragmentInitial)
    private val noBottomNavigationViewIds = setOf(R.id.fragmentSplash,R.id.fragmentInitial,R.id.fragmentLogin,R.id.fragmentRegister1,R.id.fragmentRegister2,
        R.id.fragmentRecoverPass,R.id.fragmentRunning)
    private val topLevelIds = setOf(R.id.fragmentSplash,R.id.fragmentInitial, R.id.fragmentFeed)

    lateinit var binding: ActivityMainBinding

    // VAR UI Elements
    val actionbar : ConstraintLayout by lazy { findViewById(R.id.ly_actionbar) }
    val bottomNavigationView : BottomNavigationView by lazy { findViewById(R.id.bottomNavigationView) }


    override var actionBarTitle: String
        get() = binding.actionbarTitle.text.toString()
        set(value){
            binding.actionbarTitle.text = value
        }
    val backButton : MaterialButton by lazy { binding.actionbarBack }
    val buttonsLayout : LinearLayoutCompat by lazy { binding.actionbarButtons }
    val btActivityType : MaterialButton by lazy { binding.actionbarBtActivityType }

    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        screenSplash.setKeepOnScreenCondition{false}

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToRunningFragmentIfNeeded(intent)


        with(bottomNavigationView){
            setupWithNavController(navController)
            setOnItemSelectedListener {
                navController.navigateToMenuItem(it)
            }
        }
        // Back Button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        navController.addOnDestinationChangedListener{ navController: NavController, navDestination: NavDestination, arguments: Bundle? ->

            // No retroceso
            if (navDestination.id in topLevelIds){
                navController.graph.setStartDestination(navDestination.id)
                backButton.invisible()
            }
            else
                backButton.visible()

            // Titulo
            actionBarTitle = if (arguments?.getString("title") != null)
                arguments.getString("title")!!
            else
                navDestination.label.toString()

            // Ocultar Actionbar
            actionbar.visible(navDestination.id !in noActionBarIds)
            // Ocultar BottomNavigationView
            bottomNavigationView.visible(navDestination.id !in noBottomNavigationViewIds)

            // Ocultar botones
            buttonsLayout.children.forEach {
                it.invisible()
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

    override fun onDestroy() {
        sendCommandToService(ACTION_STOP_RUNNING)
        super.onDestroy()
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