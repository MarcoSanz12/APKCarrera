package com.gf.apkcarrera.features.f2_friends.fragments

import android.view.View
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg02FriendsBinding
import com.gf.apkcarrera.features.f2_friends.adapter.FriendsViewpagerAdapter
import com.gf.common.extensions.addOnTabSelectedListener
import com.gf.common.platform.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsFragment : BaseFragment<Frg02FriendsBinding>(){

    val tabFragments = listOf(FriendsListFragment(),FriendsRequestsFragment())

    override fun initializeView() {
        setViewPager()
        setOnAddFriendClickListener(::onAddFriendClick)
    }

    private fun onAddFriendClick(view: View?) {
        navigate(R.id.action_fragmentFriends_to_fragmentAddFriends)
    }

    private fun setViewPager(){
        fun onTabSelected(tab:TabLayout.Tab?){

        }

        with(binding){
            val mAdapter = FriendsViewpagerAdapter(this@FriendsFragment,tabFragments)

            viewpager.apply {
                adapter = mAdapter
                isUserInputEnabled = false
            }

            groupBotonesTop.addOnTabSelectedListener(::onTabSelected)
            TabLayoutMediator(groupBotonesTop,viewpager){ tab: TabLayout.Tab, i: Int ->
                tab.text = if (i == 0)
                    getString(com.gf.common.R.string.tab_friends)
                else
                    getString(com.gf.common.R.string.tab_requests)
            }.attach()
        }

    }



}