package com.gf.apkcarrera.features.f4_settings.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.activityViewModels
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg04SettingsBinding
import com.gf.apkcarrera.features.f1_feed.viewmodel.MainViewModel
import com.gf.common.platform.BaseFragment

class FragmentSettings : BaseFragment<Frg04SettingsBinding>() {

    val viewModel: MainViewModel by activityViewModels()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        setOnBackPressed(R.id.fragmentFeed)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.lyLogout.setOnClickListener {
            showLoadingDialog(getString(com.gf.common.R.string.loggin_out))
            viewModel.logout {
                Log.d("Logout", "Llegamos")
                android.widget.Toast.makeText(requireContext(), getString(com.gf.common.R.string.logout_succesful), LENGTH_SHORT).show()
                hideLoadingDialog()
                navigate(R.id.action_fragmentSettings_to_fragmentInitial)
            }
        }
    }
}