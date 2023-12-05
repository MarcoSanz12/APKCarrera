package com.gf.apkcarrera.features.f2_friends.fragments

import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import com.gf.apkcarrera.MainActivity
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg02FriendsAddBinding
import com.gf.apkcarrera.features.f2_friends.adapter.NewFriendsAdapter
import com.gf.apkcarrera.features.f2_friends.viewmodel.FriendsViewModel
import com.gf.common.entity.friend.FriendModel
import com.gf.common.extensions.assignAnimatedAdapter
import com.gf.common.extensions.collectFlow
import com.gf.common.extensions.hideKeyboard
import com.gf.common.extensions.invisible
import com.gf.common.extensions.showKeyboard
import com.gf.common.extensions.textToString
import com.gf.common.extensions.toast
import com.gf.common.extensions.visible
import com.gf.common.platform.BaseFragment
import com.gf.common.response.FriendListResponse
import com.gf.common.response.FriendResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsAddFragment : BaseFragment<Frg02FriendsAddBinding>() {

    private val viewModel : FriendsViewModel by hiltNavGraphViewModels(R.id.nav_friends)
    private lateinit var adapter : NewFriendsAdapter

    private var lastSearch = ""
    private var isSearching = false


    override fun initializeView() {
        (baseActivity as MainActivity).binding.actionbarBtAddFriend.isChecked = true
        setOnAddFriendClickListener(::onAddFriendDismiss)

        adapter = NewFriendsAdapter(listOf(),::addFriendClick,::cancelFriendClick)

        // Asignamos el adaptador
        binding.rvList.assignAnimatedAdapter(
            adapter = adapter,
            animationId = com.gf.common.R.anim.animation_layout_fade_in
        )

        binding.btSearch.setOnClickListener(::onSearchClick)
        binding.tvSearch.let {
            it.setOnEditorActionListener { view, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    onSearchClick(view)
                    true
                }
                else
                    false
            }
            it.showKeyboard()
        }
    }

    override fun initObservers() {
        with (viewModel){
            collectFlow(friendAddListState,Lifecycle.State.STARTED,::onListLoaded)
            collectFlow(friendAddState,Lifecycle.State.STARTED,::onRequestSent)
            collectFlow(friendCancelState,Lifecycle.State.STARTED,::onRequestCanceled)
        }
    }

    private fun onSearchClick(view:View){
        binding.tvSearch.hideKeyboard()
        // No buscar si escribe menos de 3 caracteres
        if (binding.tvSearch.textToString().length < 3)
            snackbar(com.gf.common.R.string.search_min_chars)
        // Solo buscar si no está buscando o ya ha buscado eso
        else if (!isSearching && lastSearch != binding.tvSearch.textToString()){
            lastSearch = binding.tvSearch.textToString()
            showLoadingDialog(getString(com.gf.common.R.string.search_in_progress))
            viewModel.searchNewFriends(binding.tvSearch.textToString())
            isSearching = true
        }
    }

    private fun addFriendClick(friendModel: FriendModel) {
        if (friendModel.uid.isNotEmpty()){
            showLoadingDialog(getString(com.gf.common.R.string.friend_sending_request))
            viewModel.sendFriendRequest(friendModel.uid)
        }

    }

    private fun cancelFriendClick(friendModel: FriendModel) {
        if (friendModel.uid.isNotEmpty()){
            showLoadingDialog(getString(com.gf.common.R.string.friend_cancel_request))
            viewModel.cancelFriendRequest(friendModel.uid)
        }
    }

    // Envío de petición
    private fun onRequestSent(friendResponse: FriendResponse) {
        hideLoadingDialog()
        if (friendResponse is FriendResponse.Succesful){
            val isEmptyList = adapter.friendRequestSent(friendResponse.friendId)
            hideList(isEmptyList)
        }
        else
            toast(com.gf.common.R.string.generic_error)
    }



    // Cancelación de petición
    private fun onRequestCanceled(friendResponse: FriendResponse) {
        hideLoadingDialog()
        if (friendResponse is FriendResponse.Succesful){
            val isEmptyList = adapter.friendRequestCanceled(friendResponse.friendId)
            hideList(isEmptyList)
        }
        else
            toast(com.gf.common.R.string.generic_error)
    }

    private fun onListLoaded(friendListResponse: FriendListResponse) {
        isSearching = false
        hideLoadingDialog()
        // 1. Si llega una lista con elementos
        if ((friendListResponse as? FriendListResponse.Succesful)?.friendList?.isNotEmpty() == true && this::adapter.isInitialized){
            adapter.actualizarLista(friendListResponse.friendList)
            binding.lyResultsNotFound.invisible()
            binding.rvList.visible()
        }
        // 2. No llega nada
        else{
            binding.tvWantedName.text = lastSearch
            binding.lyResultsNotFound.visible()
            binding.rvList.invisible()
        }
    }

    private fun onAddFriendDismiss(view: View) = onBackPressed()

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