package com.gf.apkcarrera.features.f1_feed.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.cotesa.common.extensions.toBitmap
import com.gf.apkcarrera.databinding.Frg01FeedBinding
import com.gf.apkcarrera.features.f1_feed.viewmodel.MainViewModel
import com.gf.common.entity.user.UserModel
import com.gf.common.platform.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : BaseFragment<Frg01FeedBinding>() {

    val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.user.observe(this, Observer(::handleUserLoaded))
        hideLoadingDialog()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getUser()
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    private fun handleUserLoaded(userModel: UserModel?) {
        binding.apply {
            tvName.text = userModel?.name
            tvUid.text = userModel?.uid
            tvUsername.text = userModel?.username
            ivProfilePic.setImageBitmap(userModel?.picture?.toBitmap())
        }
    }
}