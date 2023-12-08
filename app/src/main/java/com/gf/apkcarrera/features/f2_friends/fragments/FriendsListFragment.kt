package com.gf.apkcarrera.features.f2_friends.fragments

import android.util.Log
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg02FriendsListBinding
import com.gf.apkcarrera.features.f2_friends.adapter.FriendsAdapter
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
class FriendsListFragment : BaseFragment<Frg02FriendsListBinding>() {

    private val viewModel : FriendsViewModel by hiltNavGraphViewModels(R.id.nav_friends)
    private lateinit var adapter : FriendsAdapter

    companion object{
        private const val TAG = "FriendsListFragment"
    }

    override fun initializeView() {
        adapter = FriendsAdapter(listOf(),::onFriendClick,::removeFriendClick)

        // Asignamos el adaptador
        binding.rvList.assignAnimatedAdapter(
            adapter = adapter,
            animationId = com.gf.common.R.anim.animation_layout_fade_in
        )

        viewModel.getFriends()
    }

    private fun onFriendClick(friendModel: FriendModel) {
        navigate(FriendsListFragmentDirections.actionGlobalNavProfile(friendModel.uid))
    }

    override fun initObservers() {
        with(viewModel){
            Log.d(TAG, "initObservers: Observando")
            collectFlow(friendListState,Lifecycle.State.STARTED,::friendListLoaded)
            collectFlow(friendRemoveState,Lifecycle.State.STARTED,::friendRemoved)
        }
    }

    private fun friendListLoaded(friendListResponse: FriendListResponse) {
        hideLoadingDialog()
        // 1. Si llega una lista con elementos
        if ((friendListResponse as? FriendListResponse.Succesful)?.friendList?.isNotEmpty() == true && this::adapter.isInitialized){
            Log.d(TAG, "Añadiendo ${friendListResponse.friendList.size} amigos")
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

    private fun removeFriendClick(friendModel: FriendModel) {
        if (friendModel.uid.isNotEmpty()){
            showLoadingDialog(getString(com.gf.common.R.string.friend_removing))
            viewModel.removeFriend(friendModel.uid)
        }
    }

    private fun friendRemoved(friendResponse: FriendResponse) {
        hideLoadingDialog()
        if (friendResponse is FriendResponse.Succesful){
            val friend = adapter.resourceListFiltered.find { it.uid == friendResponse.friendId }

            val isEmptyList = adapter.friendRemoved(friendResponse.friendId)
            snackbar(getString(com.gf.common.R.string.friend_removing_snackbar,friend?.uname ?: "???"))
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

    override fun onBackPressed() {
        baseActivity.navController.popBackStack(R.id.fragmentFeed,false)
    }


}