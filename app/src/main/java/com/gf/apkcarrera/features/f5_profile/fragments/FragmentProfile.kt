package com.gf.apkcarrera.features.f5_profile.fragments

import android.content.Context
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg05ProfileBinding
import com.gf.common.platform.BaseFragment

class FragmentProfile : BaseFragment<Frg05ProfileBinding>() {
    override fun onAttach(context: Context) {
        super.onAttach(context)
        setOnBackPressed(R.id.fragmentFeed)
    }
}