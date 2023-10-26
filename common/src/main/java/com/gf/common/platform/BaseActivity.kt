package com.gf.common.platform

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
        loadingDialog?.cancel()
    }

}