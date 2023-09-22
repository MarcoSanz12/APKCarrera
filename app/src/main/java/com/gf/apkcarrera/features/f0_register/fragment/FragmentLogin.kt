package com.gf.apkcarrera.features.f0_register.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.gf.apkcarrera.databinding.Frg00LoginBinding
import com.gf.apkcarrera.features.f0_register.viewmodel.RegisterViewModel
import com.gf.common.R
import com.gf.common.entity.user.LoginRequest
import com.gf.common.entity.user.UserModel
import com.gf.common.extensions.isEmpty
import com.gf.common.extensions.isValidEmail
import com.gf.common.extensions.textToString
import com.gf.common.platform.BaseFragment
import com.gf.common.utils.Constants

class FragmentLogin : BaseFragment<Frg00LoginBinding>() {

    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.failure.observe(this,::handleFailure)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initializeView()
    }

    private fun Frg00LoginBinding.initializeView(){
        btLogin.setOnClickListener {
            if (verifyFields()){
                showLoadingDialog(getString(R.string.loading_signing_in))
                viewModel.login(
                    LoginRequest().apply {
                        email = etEmail.textToString()
                        password = etPassword.textToString()
                    },
                    ::handleUserLogin
                )
            }

        }

        tvForgotPassword.setOnClickListener{
            navigate(com.gf.apkcarrera.R.id.action_fragmentLogin_to_fragmentRecoverPass)
        }
    }

    private fun handleUserLogin(userModel: UserModel) {
        saveUserData(userModel)
        hideLoadingDialog()
        navigate(com.gf.apkcarrera.R.id.action_global_navigationMain)
    }

    private fun saveUserData(userModel: UserModel){
        putPreference(Constants.Login.ALWAYS_LOGGED,binding.cbKeepLogged.isChecked)
        putPreference(Constants.Login.LOG_EMAIL, binding.etEmail.textToString())
        putPreference(Constants.Login.LOG_PASSWORD,binding.etPassword.textToString())
        putPreference(Constants.Login.LOG_UID,userModel.uid)
    }

    private fun Frg00LoginBinding.verifyFields() : Boolean{
        var isValid = true
        if (!etEmail.textToString().isValidEmail()){
            etEmail.error = getString(R.string.verify_email_not_valid)
            isValid = false
        }

        if (etPassword.isEmpty()){
            etPassword.error = getString(R.string.verify_field_required)
            isValid = false
        }else if (etPassword.length() < 6){
            etPassword.error = getString(R.string.verify_password_not_long)
            isValid = false
        }

        return isValid
    }
}