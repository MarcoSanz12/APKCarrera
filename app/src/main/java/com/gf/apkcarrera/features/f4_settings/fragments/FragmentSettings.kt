package com.gf.apkcarrera.features.f4_settings.fragments

import android.content.Context
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg04SettingsBinding
import com.gf.common.platform.BaseFragment

class FragmentSettings : BaseFragment<Frg04SettingsBinding>() {
    override fun onAttach(context: Context) {
        super.onAttach(context)
        setOnBackPressed(R.id.fragmentFeed)
    }
}