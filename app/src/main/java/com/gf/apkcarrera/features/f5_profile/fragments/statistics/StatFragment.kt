package com.gf.apkcarrera.features.f5_profile.fragments.statistics

import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.gf.apkcarrera.databinding.Frg05ProfileStatsBinding
import com.gf.apkcarrera.features.f2_friends.adapter.FriendsViewpagerAdapter
import com.gf.apkcarrera.features.f2_friends.fragments.FriendsListFragment
import com.gf.apkcarrera.features.f2_friends.fragments.FriendsRequestsFragment
import com.gf.apkcarrera.features.f5_profile.adapter.StatsAdapter
import com.gf.apkcarrera.features.f5_profile.viewmodel.ProfileViewModel
import com.gf.common.R
import com.gf.common.entity.activity.ActivityType
import com.gf.common.extensions.addOnTabSelectedListener
import com.gf.common.platform.BaseFragment
import com.gf.common.platform.BaseViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

class StatFragment() : BaseFragment<Frg05ProfileStatsBinding>() {

    val tabFragments = listOf(
        StatDataFragment(ActivityType.WALK),
        StatDataFragment(ActivityType.RUN),
        StatDataFragment(ActivityType.BIKE))

    override fun initializeView() {
        setViewPager()
    }

    private fun setViewPager(){
        fun onTabSelected(tab: TabLayout.Tab?){

        }

        with(binding){
            val mAdapter = StatsAdapter(this@StatFragment,tabFragments)

            viewpager.apply {
                adapter = mAdapter
            }

            groupBotonesTop.addOnTabSelectedListener(::onTabSelected)
            TabLayoutMediator(groupBotonesTop,viewpager){ tab: TabLayout.Tab, i: Int ->
                tab.setCustomView(com.gf.apkcarrera.R.layout.custom_tab)
                // Andar
                tab.icon = if (i == 0)
                    ResourcesCompat.getDrawable(resources,R.drawable.icon_walk,null)
                // Correr
                else if (i == 1)
                    ResourcesCompat.getDrawable(resources,R.drawable.icon_run,null)
                // Bicicleta
                else
                    ResourcesCompat.getDrawable(resources,R.drawable.icon_bike,null)
            }.attach()
        }

    }
}