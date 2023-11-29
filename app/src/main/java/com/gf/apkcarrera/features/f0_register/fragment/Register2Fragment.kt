package com.gf.apkcarrera.features.f0_register.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.cotesa.common.extensions.notNull
import com.cotesa.common.extensions.toBase64
import com.gf.apkcarrera.databinding.Frg00Register2Binding
import com.gf.apkcarrera.features.f0_register.viewmodel.RegisterViewModel
import com.gf.common.R
import com.gf.common.entity.user.LoginRequest
import com.gf.common.entity.user.UserModel
import com.gf.common.extensions.textToString
import com.gf.common.platform.BaseCameraFragment
import com.gf.common.utils.Constants.Login.ALWAYS_LOGGED
import com.gf.common.utils.Constants.Login.LOG_EMAIL
import com.gf.common.utils.Constants.Login.LOG_PASSWORD
import com.gf.common.utils.Constants.Login.LOG_UID
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import java.util.Random

@AndroidEntryPoint
class Register2Fragment : BaseCameraFragment<Frg00Register2Binding>() {

    private val viewModel: RegisterViewModel by activityViewModels()
    private val user by lazy {viewModel.request ?: LoginRequest() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.failure.observe(this,::handleFailure)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignProfilePicture()
    }

    override fun initializeView(){
        binding.apply {

            etName.setText(user.username)

            ivProfilePic.setOnClickListener {
                try{
                    uploadImage(195,195)

                }catch (ex: Throwable){
                    ex.printStackTrace()
                }
            }

            btAccept.setOnClickListener(::clickAccept)
        }
    }

    private fun clickAccept(view: View?) {
        binding.apply {
            val request = viewModel.request
            if (etName.textToString().length < 6){
                etName.error = getString(com.gf.common.R.string.verify_name_not_long)
                etName.setText(request?.username ?: "Guillermo Díaz Ibañez")
                return
            }

            request!!.apply {
                name = etName.textToString()
                searchname = name.trim().lowercase(Locale.getDefault())
            }

            showLoadingDialog(getString(R.string.loading_signing_up))
            viewModel.register(request,::handleUserRegistered)
        }
    }

    private fun handleUserRegistered(userModel: UserModel) {
        hideLoadingDialog()
        saveUserData(userModel.uid)
        navigate(com.gf.apkcarrera.R.id.action_global_navigationMain)
    }

    override fun onImageLoadedListener(img: Bitmap?) {
        img.notNull { image ->
            binding.ivProfilePic.setImageBitmap(image)
            viewModel.request?.picture = image.toBase64()
        }

    }

    private fun assignProfilePicture(){
        val pResource =
            if (Random().nextInt(2) == 0)
                R.drawable.m_profile
            else
                R.drawable.f_profile

          onImageLoadedListener(BitmapFactory.decodeResource(requireContext().resources,pResource))
    }

    private fun saveUserData(uid: String){
        putPreference(ALWAYS_LOGGED,binding.cbKeepLogged.isChecked)
        putPreference(LOG_EMAIL, viewModel.request!!.email)
        putPreference(LOG_PASSWORD,viewModel.request!!.password)
        putPreference(LOG_UID,uid)
    }

}