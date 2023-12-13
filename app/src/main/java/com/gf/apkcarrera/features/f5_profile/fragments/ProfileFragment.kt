package com.gf.apkcarrera.features.f5_profile.fragments

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.navArgs
import com.cotesa.common.extensions.toBitmap
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.NavProfileArgs
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg05ProfileBinding
import com.gf.apkcarrera.features.f5_profile.viewmodel.ProfileViewModel
import com.gf.common.dialog.UpdateProfileDialog
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.gf.common.extensions.collectFlowOnce
import com.gf.common.platform.BaseCameraFragment
import com.gf.common.platform.BaseFragment
import com.gf.common.response.ProfileResponse
import com.gf.common.response.ProfileUpdateResponse
import com.gf.common.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseCameraFragment<Frg05ProfileBinding>() {

    private val viewModel : ProfileViewModel by hiltNavGraphViewModels(R.id.nav_profile)
    private val args : NavProfileArgs by navArgs()

    private lateinit var profile : UserModel
    private lateinit var activities : List<ActivityModel>

    private val userid by lazy{preferences.getString(Constants.Login.LOG_UID,null)}

    private lateinit var updateProfileDialog : UpdateProfileDialog

    override fun initObservers() {
        with(viewModel){
            collectFlowOnce(profileState,::profileLoaded)
            collectFlowOnce(profileUpdatedState,::profileUpdated)
            getProfile(args.uid)
        }
    }

    override fun initializeView() {
        // Si ya teniamos el Perfil cargado, pues cargarlo
        if (this::profile.isInitialized)
            loadProfile()
        // Si no, hacer la llamada
        else
            viewModel.getProfile(args.uid)
    }

    private fun profileLoaded(profileResponse: ProfileResponse) {
        hideLoadingDialog()
        if (profileResponse is ProfileResponse.Succesful){
            profile = profileResponse.user
            activities = profileResponse.activityList
            loadProfile()
        }
        else
            error()
    }

    override fun onImageLoadedListener(img: Bitmap?) {
        img?.let {
            updateProfileDialog.changeImage(img)
        }
    }

    private fun profileUpdated(profileUpdateResponse: ProfileUpdateResponse) {
        when (profileUpdateResponse){
            ProfileUpdateResponse.Error -> {
                hideLoadingDialog()
                updateProfileDialog.dismiss()
                snackbar(com.gf.common.R.string.generic_error)
            }
            is ProfileUpdateResponse.Succesful -> {
                profile.name = profileUpdateResponse.updatename
                profile.picture = profileUpdateResponse.updatepicture
                viewModel.profileUpdated(profile)
                updateProfileDialog.dismiss()
            }
        }
    }

    private fun loadProfile(){
        actionBarTitle = if (userid == profile.uid)
            getString(com.gf.common.R.string.yo)
        else
            profile.name

        try{
            with (binding){

                // Solo edición si es el propio usuario
                if (userid == profile.uid)
                    (requireActivity() as MainActivity).setOnPencilByClickListener{
                        updateProfileDialog = UpdateProfileDialog(requireContext(),profile.picture,profile.name,::onUpdateProfile,::onImageUpdateClick)
                        updateProfileDialog.show()
                    }


                // Nombre
                tvName.text = profile.name

                // Foto
                ivProfilePic.setImageBitmap(profile.picture.toBitmap())
                ivProfilePic.setOnClickListener {
                    (requireActivity() as MainActivity).showZoomableImage(profile.picture.toBitmap())
                }

                // Estadisticas
                btStats.setOnClickListener { navigate(R.id.action_fragmentProfile_to_statFragment) }

                // Actividades
                btActivities.setOnClickListener{navigate(ProfileFragmentDirections.actionGlobalNavFeed(profile.uid))}
            }
        }catch (ex: Exception){
            ex.printStackTrace()
        }
    }

    private fun onImageUpdateClick() =
        uploadImage()


    private fun onUpdateProfile(name: String, picture: String) {
        viewModel.updateProfile(profile.uid,name,picture)
        showLoadingDialog(getString(com.gf.common.R.string.loading_signing_up))
    }

    private fun error(){
        snackbar(com.gf.common.R.string.generic_error)
        hideLoadingDialog()
        onBackPressed()
    }

    override fun onBackPressed() {
        if (args.uid == null)
            baseActivity.navController.popBackStack(R.id.fragmentFeed,false)
        else
            super.onBackPressed()
    }



}