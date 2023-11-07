package com.gf.apkcarrera

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.children
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUiSaveStateControl
import com.gf.apkcarrera.databinding.ActivityMainBinding
import com.gf.apkcarrera.features.f3_running.service.RunningService
import com.gf.common.dialog.MultimediaDialog
import com.gf.common.extensions.invisible
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
        R.id.fragmentRecoverPass,R.id.fragmentRunning, R.id.fragmentRunningEnd)
    private val topLevelIds = setOf(R.id.fragmentSplash,R.id.fragmentInitial, R.id.fragmentFeed)

    lateinit var binding: ActivityMainBinding

    // VAR UI Elements
    val actionbar : ConstraintLayout by lazy { findViewById(R.id.ly_actionbar) }
    val bottomNavigationView : BottomNavigationView by lazy { findViewById(R.id.bottomNavigationView) }

    // Galeria de fotos
    private var multimediaDialog : MultimediaDialog? = null


    override var actionBarTitle: String
        get() = binding.actionbarTitle.text.toString()
        set(value){
            binding.actionbarTitle.text = value
        }
    val backButton : MaterialButton by lazy { binding.actionbarBack }
    val buttonsLayout : LinearLayoutCompat by lazy { binding.actionbarButtons }
    val btActivityType : MaterialButton by lazy { binding.actionbarBtActivityType }

    @OptIn(NavigationUiSaveStateControl::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        screenSplash.setKeepOnScreenCondition{false}

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToRunningFragmentIfNeeded(intent)

        NavigationUI.setupWithNavController(binding.bottomNavigationView,navController,false)

            /*Navegación bug
                with(bottomNavigationView){
                    setupWithNavController(navController)
                    setOnItemSelectedListener {
                        navController.navigateToMenuItem(it)
                    }
                }
                */


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

    /**
     * Abre [MultimediaDialog] cargando un [Multimedia]
     *
     * @param images Imágen a cargar
     *
     * @return **True** si no es null o **False** si es null
     */
    fun showZoomableImage(image : Bitmap?) : Boolean =
        if (image != null)
            showZoomableImage(listOf(image))
        else
            false

    /**
     * Abre [MultimediaDialog] cargando una [List] de [Bitmap] y abre por defecto
     * el que este en la posicion [position]
     *
     * @param images Lista de imágenes a cargar
     * @param position Posición de la primera imágen que se abrirá
     *
     * @return **True** si la lista no esta vacía o **False** si la lista está vacía
     */
    fun showZoomableImage(images : List<Bitmap>, position : Int = 0) : Boolean {
        return if (images.isEmpty() || multimediaDialog?.isShowing == true)
            false
        else{
            multimediaDialog = MultimediaDialog(this,images,position)
            multimediaDialog!!.show()
            true
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
        Intent(this, RunningService::class.java).also {
            it.action = action
            this.startService(it)
        }
}