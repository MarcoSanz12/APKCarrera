package com.gf.apkcarrera.features.f0_register.fragment

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.gf.apkcarrera.databinding.Frg00Register1Binding
import com.gf.apkcarrera.features.f0_register.viewmodel.RegisterViewModel
import com.gf.common.R
import com.gf.common.entity.user.LoginRequest
import com.gf.common.extensions.isEmpty
import com.gf.common.extensions.isValidEmail
import com.gf.common.extensions.textToString
import com.gf.common.extensions.toast
import com.gf.common.platform.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Register1Fragment : BaseFragment<Frg00Register1Binding>() {

    val viewModel: RegisterViewModel by activityViewModels()

    private var debugCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel.failure.observe(this,Observer{
            Toast.makeText(requireContext(), "Se produjo un fallo", Toast.LENGTH_SHORT).show()
            viewModel.request = null
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btRegister.setOnClickListener {
                if (validateUser()){
                    val user = LoginRequest().apply {
                        username = etUsername.textToString()
                        password = etPassword.textToString()
                        email = etEmail.textToString()
                    }
                    viewModel.request = user
                    viewModel.checkUserExists(user,::handleUserExists)
                }
            }

            ivLogo.setOnClickListener {
                debugCounter++
                if (debugCounter == 3){
                    toast("Auto user 1")
                    etUsername.setText("prueba1")
                    etEmail.setText("prueba1@gmail.com")
                    etPassword.setText("prueba")
                    etRepeatPassword.setText("prueba")
                }
                else if (debugCounter == 4){
                    toast("Auto user 2")
                    etUsername.setText("prueba2")
                    etEmail.setText("prueba2@gmail.com")
                    etPassword.setText("prueba")
                    etRepeatPassword.setText("prueba")
                }
                else if (debugCounter > 4){
                    etUsername.setText("")
                    etEmail.setText("")
                    etPassword.setText("")
                    etRepeatPassword.setText("")
                    debugCounter = 0
                }
            }
        }
    }


    private fun handleUserExists(exists: Boolean) {
            if (exists){
                Toast.makeText(requireContext(), getString(R.string.user_exists), Toast.LENGTH_SHORT).show()
                binding.apply {
                    etEmail.setText("")
                    etEmail.error = getString(R.string.verify_field_already_used)
                    etUsername.setText("")
                    etUsername.error = getString(R.string.verify_field_already_used)
                }
            }
            else
                navigate(com.gf.apkcarrera.R.id.action_fragmentRegister1_to_fragmentRegister2)
        }

    private fun validateUser() : Boolean {
        binding.apply {

            // Campos no vacios
            if (!(etEmail.isNotEmpty()
                ||
                etUsername.isNotEmpty()
                ||
                etPassword.isNotEmpty()
                ||
                etRepeatPassword.isNotEmpty()))
                return false

            // Email válido
            if (!etEmail.textToString().isValidEmail()) {
                etEmail.error = getString(R.string.verify_email_not_valid)
                return false
            }

            // Contraseña y verificar contraseña no encajan
            if (etPassword.textToString() != etRepeatPassword.textToString()){
                getString(R.string.verify_password_not_match).apply {
                    etPassword.error = this
                    etRepeatPassword.error = this
                }
                return false
            }

            // Username y contraseña más de 6 caracteres
            if (!etUsername.isLongEnough(getString(R.string.verify_username_not_long))
                ||
                !etPassword.isLongEnough(getString(R.string.verify_password_not_long))
                ||
                !etRepeatPassword.isLongEnough(getString(R.string.verify_password_not_long)))
                return false

            if (!cbTermsConditions.isChecked){
                Toast.makeText(requireContext(), getString(R.string.verify_terms_condicions), Toast.LENGTH_SHORT).show()
                return false
            }

            return true
        }
    }

    private fun EditText.isNotEmpty() : Boolean{
        if (this.isEmpty())
            this.error = getString(R.string.verify_field_required)

        return !this.isEmpty()

    }

    private fun EditText.isLongEnough(msg : String) : Boolean{
        if (this.textToString().length < 6)
            this.error = msg

        return this.textToString().length >= 6
    }
}