package com.gf.apkcarrera.features.f1_feed.fragment

import android.util.Log
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg01FeedBinding
import com.gf.apkcarrera.features.f1_feed.adapter.FeedAdapter
import com.gf.apkcarrera.features.f1_feed.viewmodel.FeedViewModel
import com.gf.common.entity.feed.FeedImage
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.extensions.collectFlowOnce
import com.gf.common.extensions.toast
import com.gf.common.platform.BaseFragment
import com.gf.common.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedFragment : BaseFragment<Frg01FeedBinding>() {

    private val args : FeedFragmentArgs by navArgs()

    companion object{
        private const val TAG = "FeedFragment"
    }

    private val viewModel: FeedViewModel by hiltNavGraphViewModels(R.id.nav_feed)
    private lateinit var adapter : FeedAdapter

    private val userId by lazy{
        preferences.getString(Constants.Login.LOG_UID,"-777") ?: "-777"
    }


    override fun initObservers() {
        with(viewModel){
            collectFlowOnce(failureState,::onFeedFailure)
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    viewModel._feedFlow?.collectLatest { pagingData ->
                        Log.d(TAG, "Recogiendo FLOW")
                        adapter.submitData(pagingData)
                    }
                }
            }
        }
    }
    override fun initializeView() {
        hideLoadingDialog()

        adapter = FeedAdapter(userId,::onImageClick,::onProfileClick)
        binding.rvList.adapter = adapter

        viewModel.getFeedActivities(args.uid)
    }

    private fun onProfileClick(userModel: UserModel) {
        navigate(FeedFragmentDirections.actionGlobalNavProfile(userModel.uid))
    }

    private fun onImageClick(bitmaps: List<FeedImage>, i: Int) {
        (requireActivity() as MainActivity).showZoomableImage(urls = bitmaps.map { it.url }, position = i)
    }

    private fun onFeedFailure(failure: Failure) {
        toast(com.gf.common.R.string.generic_error)
    }

    /*    override fun onBackPressed() {
            requireActivity().finish()
        }
        */



}