package com.gf.common.platform

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.gf.common.dialog.LoadingDialog

abstract class BaseActivity : AppCompatActivity() {

    abstract val navController : NavController
    abstract val actionBarTitle : String
    private var loadingDialog : LoadingDialog? = null

    fun showLoadingDialog(msg:String){
        if (loadingDialog == null)
            loadingDialog = LoadingDialog(this,msg)
        else
            loadingDialog!!.setText(msg)

        loadingDialog!!.show()
    }

    fun hideLoadingDialog(){
        loadingDialog?.dismiss()
    }

    /**
     * Usar para asignar un [View.OnClickListener] al botón Tipo de Actividad
     */
    abstract fun setOnActivityTypeByClickListener(listener: View.OnClickListener)

    /**
     * Usar para asignar un [View.OnClickListener] al botón Añadir amigo
     */
    abstract fun setOnAddFriendByClickListener(listener: View.OnClickListener)

    /**
     * Usar para asignar un [View.OnClickListener] al botón Buscador
     */
    abstract fun setOnSearchByClickListener(listener: View.OnClickListener)

    /**
     * Usar para asignar un [View.OnClickListener] al botón Filtro
     */
    abstract fun setOnFilterByClickListener(listener: View.OnClickListener)

    /**
     * Usar para asignar un [View.OnClickListener] al botón Guardar
     */
    abstract fun setOnSaveByClickListener(listener: View.OnClickListener)

    /**
     * Usar para asignar un [View.OnClickListener] al botón Bandera
     */
    abstract fun setOnFlagByClickListener(listener: View.OnClickListener)

    /**
     * Usar para asignar un [View.OnClickListener] al botón Borrar
     */
    abstract fun setOnDeleteByClickListener(listener: View.OnClickListener)



}