package com.gf.apkcarrera.features.f2_friends.fragments

import android.content.Context
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg02FriendsBinding
import com.gf.common.platform.BaseFragment

class FragmentFriends : BaseFragment<Frg02FriendsBinding>(){

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setOnBackPressed(R.id.fragmentFeed)
    }

}