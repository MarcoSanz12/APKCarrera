package com.gf.apkcarrera

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.gf.common.entity.activity.ActivityStatus
import com.gf.common.entity.activity.ActivityType
import com.gf.common.extensions.invisible
import com.gf.common.extensions.navigateToMenuItem
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseActivity
import com.gf.common.utils.Constants.ACTION_END_RUNNING
import com.gf.common.utils.Constants.ACTION_SHOW_RUNNING_FRAGMENT
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

    var activityType = ActivityType.RUN

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


    @OptIn(NavigationUiSaveStateControl::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        screenSplash.setKeepOnScreenCondition{false}


        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToRunningFragmentIfNeeded(intent)

        // Navegación OG
        NavigationUI.setupWithNavController(binding.bottomNavigationView,navController,false)


       /* Navegación experimental custom*/
        /*with(bottomNavigationView){
                setOnItemSelectedListener {
                    navController.navigateToMenuItem(it)
                }
            }*/


        // Back Button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        navController.addOnDestinationChangedListener{ navController: NavController, navDestination: NavDestination, arguments: Bundle? ->

            // No retroceso
            if (navDestination.id in topLevelIds){
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

                // Uncheck a todos los botones
                if ((it as? MaterialButton)?.isCheckable == true){
                    it.isChecked = false
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()


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
    fun showZoomableImage(images : List<Bitmap>? = null, urls: List<String>? = null, position : Int = 0) : Boolean {
        return if ((images.isNullOrEmpty() && urls.isNullOrEmpty()) || multimediaDialog?.isShowing == true)
            false
        else{
            multimediaDialog = MultimediaDialog(this,images,urls,position)
            multimediaDialog!!.show()
            true
        }

    }


    private fun MaterialButton.setOnActionBarClickListener(listener: View.OnClickListener) {
        this.apply {
            visible()
            setOnClickListener(listener)
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
        if (RunningService.status == ActivityStatus.RUNNING)
            sendCommandToService(ACTION_END_RUNNING)
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

    // Listeners de botones

    /**
     * Asigna un [View.OnClickListener] al botón Filtro y lo pone visible
     */
    override fun setOnFilterByClickListener(listener: View.OnClickListener) =
        binding.actionbarBtFilter.setOnActionBarClickListener(listener)

    /**
     * Asigna un [View.OnClickListener] al botón Buscar y lo pone visible
     */
    override fun setOnSearchByClickListener(listener: View.OnClickListener) =
        binding.actionbarBtSearch.setOnActionBarClickListener(listener)

    /**
     * Asigna un [View.OnClickListener] al botón Activity Type y lo pone visible
     */
    override fun setOnActivityTypeByClickListener(listener: View.OnClickListener) {
        binding.actionbarBtActivityType.setOnActionBarClickListener(listener)
    }

    /**
     * Asigna un [View.OnClickListener] al botón Añadir amigo y lo pone visible
     */
    override fun setOnAddFriendByClickListener(listener: View.OnClickListener) {
        binding.actionbarBtAddFriend.setOnActionBarClickListener(listener)
    }

    /**
     * Asigna un [View.OnClickListener] al botón Bandera y lo pone visible
     */
    override fun setOnFlagByClickListener(listener: View.OnClickListener) {
        binding.actionbarBtFlag.setOnActionBarClickListener(listener)
    }

    /**
     * Asigna un [View.OnClickListener] al botón Guardar y lo pone visible
     */
    override fun setOnSaveByClickListener(listener: View.OnClickListener) {
        binding.actionbarBtSave.setOnActionBarClickListener(listener)
    }

    /**
     * Asigna un [View.OnClickListener] al botón Borrar y lo pone visible
     */
    override fun setOnDeleteByClickListener(listener: View.OnClickListener) {
        binding.actionbarBtDelete.setOnActionBarClickListener(listener)
    }
}