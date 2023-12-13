package com.gf.apkcarrera.features.f4_settings.fragments

import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg04SettingsBinding
import com.gf.apkcarrera.features.f1_feed.viewmodel.MainViewModel
import com.gf.common.platform.BaseFragment
import com.gf.common.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<Frg04SettingsBinding>() {

    val viewModel: MainViewModel by viewModels()
    override fun initializeView() {

        // Ayuda
        binding.lyHelp.setOnClickListener {
            snackbar("Lo siento, ayuda no disponible")
        }

        // Idioma
        binding.lyLanguage.setOnClickListener {
            navigate(R.id.action_fragmentSettings_to_settingsLanguageFragment)
        }
        // Logout
        binding.lyLogout.setOnClickListener {
            showLoadingDialog(getString(com.gf.common.R.string.
            loggin_out))
            viewModel.logout {
                android.widget.Toast.makeText(requireContext(), getString(com.gf.common.R.string.logout_succesful), LENGTH_SHORT).show()
                hideLoadingDialog()
                (requireActivity() as MainActivity).sendCommandToService(Constants.ACTION_END_RUNNING)
                navigate(R.id.action_global_fragmentInitial)
            }
        }
    }

    override fun onBackPressed() {
        baseActivity.navController.popBackStack(R.id.fragmentFeed,false)
    }
}