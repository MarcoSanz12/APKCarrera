package com.gf.apkcarrera.features.f5_profile.fragments

import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.navArgs
import com.cotesa.common.extensions.toBitmap
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg05ProfileBinding
import com.gf.apkcarrera.features.f5_profile.viewmodel.ProfileViewModel
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.gf.common.extensions.collectFlowOnce
import com.gf.common.platform.BaseFragment
import com.gf.common.response.ProfileResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<Frg05ProfileBinding>() {

    private val viewModel : ProfileViewModel by hiltNavGraphViewModels(R.id.nav_profile)
    private val args : ProfileFragmentArgs by navArgs()

    private lateinit var profile : UserModel
    private lateinit var activities : List<ActivityModel>

    override fun initObservers() {
        with(viewModel){
            collectFlowOnce(profileState,::profileLoaded)
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
        if (profileResponse is ProfileResponse.Succesful){
            profile = profileResponse.user
            activities = profileResponse.activityList
            loadProfile()
        }
        else
            error()
    }

    private fun loadProfile(){
        with (binding){
            // Nombre
            tvName.text = profile.name

            // Foto
            ivProfilePic.setImageBitmap(profile.picture.toBitmap())
        }
    }
    private fun error(){
        snackbar(com.gf.common.R.string.generic_error)
        onBackPressed()
    }



}