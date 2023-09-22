package com.gf.apkcarrera.features.f0_register.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg00SplashBinding
import com.gf.apkcarrera.features.f0_register.viewmodel.RegisterViewModel
import com.gf.apkcarrera.features.f1_feed.viewmodel.MainViewModel
import com.gf.common.entity.user.LoginRequest
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.extensions.invisible
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseFragment
import com.gf.common.utils.Constants.Login.ALWAYS_LOGGED
import com.gf.common.utils.Constants.Login.LOG_EMAIL
import com.gf.common.utils.Constants.Login.LOG_PASSWORD
import com.gf.common.utils.Constants.Login.LOG_UID
import com.google.android.material.bottomnavigation.BottomNavigationView

class FragmentSplash : BaseFragment<Frg00SplashBinding>() {

    private val registerVM: RegisterViewModel by activityViewModels()
    private val mainVM : MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerVM.failure.observe(this,::handleFailure)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!autoLogin())
            handleFailure(Failure.LoginError)
    }

    private fun autoLogin() : Boolean{
        if (!preferences.getBoolean(ALWAYS_LOGGED,false))
            return false

        val email = preferences.getString(LOG_EMAIL,null)
        val pass = preferences.getString(LOG_PASSWORD,null)

        if (email.isNullOrEmpty() || pass.isNullOrEmpty())
            return false

        registerVM.login(
            LoginRequest().apply {
                this.email = email
                this.password = pass
            },
            ::handleUserLogin
        )
        return true
    }

    private fun handleUserLogin(userModel: UserModel) {
        navigate(R.id.action_global_navigationMain)
        putPreference(LOG_UID,userModel.uid)
    }

    override fun handleFailure(failure: Failure?) {
        navigate(R.id.action_global_fragmentInitial)
    }

}