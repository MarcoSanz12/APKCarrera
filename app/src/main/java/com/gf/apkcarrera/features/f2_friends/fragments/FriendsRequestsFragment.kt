package com.gf.apkcarrera.features.f2_friends.fragments

import android.util.Log
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg02FriendsRequestBinding
import com.gf.apkcarrera.features.f2_friends.adapter.FriendsRequestsAdapter
import com.gf.apkcarrera.features.f2_friends.viewmodel.FriendsViewModel
import com.gf.common.entity.friend.FriendModel
import com.gf.common.extensions.assignAnimatedAdapter
import com.gf.common.extensions.collectFlow
import com.gf.common.extensions.invisible
import com.gf.common.extensions.toast
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseFragment
import com.gf.common.response.FriendListResponse
import com.gf.common.response.FriendResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsRequestsFragment : BaseFragment<Frg02FriendsRequestBinding>() {

    private val viewModel : FriendsViewModel by hiltNavGraphViewModels(R.id.nav_friends)
    private lateinit var adapter : FriendsRequestsAdapter

    companion object{
        private const val TAG = "FriendsRequestsFragment"
    }

    override fun initializeView() {
        adapter = FriendsRequestsAdapter(listOf(),::acceptFriendClick,::ignoreFriendClick)

        // Asignamos el adaptador
        binding.rvList.assignAnimatedAdapter(
            adapter = adapter,
            animationId = com.gf.common.R.anim.animation_layout_fade_in
        )

        viewModel.searchFriendRequests()
    }

    override fun initObservers() {
        with (viewModel){
            Log.d(TAG, "initObservers: Observando")
            collectFlow(friendRequestsListState,Lifecycle.State.STARTED,::onListLoaded)
            collectFlow(friendRequestOkState,Lifecycle.State.STARTED,::onRequestOk)
            collectFlow(friendRequestIgnoreState,Lifecycle.State.STARTED,::onRequestIgnored)
        }
    }


    private fun acceptFriendClick(friendModel: FriendModel) {
        if (friendModel.uid.isNotEmpty()){
            showLoadingDialog(getString(com.gf.common.R.string.friend_accepting_request))
            viewModel.acceptFriendRequest(friendModel.uid)
        }
    }
    private fun ignoreFriendClick(friendModel: FriendModel) {
        if (friendModel.uid.isNotEmpty()){
            showLoadingDialog(getString(com.gf.common.R.string.friend_ignoring_request))
            viewModel.ignoreFriendRequest(friendModel.uid)
        }
    }

    private fun onListLoaded(friendListResponse: FriendListResponse) {
        hideLoadingDialog()
        // 1. Si llega una lista con elementos
        if ((friendListResponse as? FriendListResponse.Succesful)?.friendList?.isNotEmpty() == true && this::adapter.isInitialized){
            adapter.actualizarLista(friendListResponse.friendList)
            binding.lyResultsNotFound.invisible()
            binding.rvList.visible()
        }
        // 2. No llega nada
        else{
            binding.lyResultsNotFound.visible()
            binding.rvList.invisible()
        }
    }

    private fun onRequestOk(friendResponse: FriendResponse) {
        // Actualizamos el flow de la lista de amigos

        if (friendResponse is FriendResponse.Succesful){
            Log.d(TAG, "onRequestOk: Aceptaste a ${adapter.resourceListFiltered.find { it.uid == friendResponse.friendId}?.uname }")
            viewModel.friendAccepted(adapter.resourceListFiltered.find { it.uid == friendResponse.friendId }!!)
        }


        onRequestIgnored(friendResponse)
    }

    private fun onRequestIgnored(friendResponse: FriendResponse) {
        hideLoadingDialog()
        if (friendResponse is FriendResponse.Succesful){
            val isEmptyList = adapter.friendManaged(friendResponse.friendId)
            hideList(isEmptyList)
        }
        else
            toast(com.gf.common.R.string.generic_error)
    }

    private fun hideList(isEmptyList : Boolean){
        if (isEmptyList){
            binding.lyResultsNotFound.visible()
            binding.rvList.invisible()
        }
        else{
            binding.lyResultsNotFound.invisible()
            binding.rvList.visible()
        }
    }



}