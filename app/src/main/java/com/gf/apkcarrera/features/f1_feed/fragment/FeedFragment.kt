package com.gf.apkcarrera.features.f1_feed.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import androidx.paging.filter
import androidx.paging.flatMap
import androidx.paging.log
import com.cotesa.common.extensions.toBitmap
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg01FeedBinding
import com.gf.apkcarrera.features.f1_feed.adapter.FeedAdapter
import com.gf.apkcarrera.features.f1_feed.viewmodel.FeedViewModel
import com.gf.apkcarrera.features.f1_feed.viewmodel.MainViewModel
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.extensions.collectFlow
import com.gf.common.extensions.collectFlowOnce
import com.gf.common.extensions.toast
import com.gf.common.platform.BaseFragment
import com.gf.common.response.FeedResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedFragment : BaseFragment<Frg01FeedBinding>() {

    companion object{
        private const val TAG = "FeedFragment"
    }

    private val viewModel: FeedViewModel by hiltNavGraphViewModels(R.id.nav_feed)
    private lateinit var adapter : FeedAdapter


    override fun initObservers() {
        with(viewModel){
            collectFlowOnce(failureState,::onFeedFailure)
        }
    }
    override fun initializeView() {
        hideLoadingDialog()

        adapter = FeedAdapter()
        binding.rvList.adapter = adapter

        viewModel.getFeedActivities()

        lifecycleScope.launch {
            viewModel._feedFlow?.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun onFeedFailure(failure: Failure) {
        toast(com.gf.common.R.string.generic_error)
    }

/*    override fun onBackPressed() {
        requireActivity().finish()
    }*/



}