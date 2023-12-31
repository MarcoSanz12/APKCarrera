package com.gf.apkcarrera.features.f0_register.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg00InitialBinding
import com.gf.common.platform.BaseFragment

class FragmentInitial : BaseFragment<Frg00InitialBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btLogin.setOnClickListener {
                navigate(R.id.action_fragmentInitial_to_fragmentLogin)
            }
            btRegister.setOnClickListener {
               navigate(R.id.action_fragmentInitial_to_fragmentRegister1)
            }

            tvVersion.text = getString(com.gf.common.R.string.version,getAppVersion(requireContext()))
        }
    }

    private fun getAppVersion(context: Context): String {
        val packageName = context.packageName
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "N/A"
        }
    }
}