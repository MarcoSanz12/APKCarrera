package com.gf.apkcarrera.features.f5_profile.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatsAdapter (fragment: Fragment, val fragmentList : List<Fragment>) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}