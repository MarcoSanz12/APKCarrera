package com.gf.apkcarrera.features.f2_friends.viewmodel

import com.gf.apkcarrera.features.f2_friends.usecase.AddFriendUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.SearchFriendsUseCase
import com.gf.common.platform.BaseViewModel
import com.gf.common.response.FriendListResponse
import com.gf.common.response.FriendResponse
import com.gf.common.response.GenericResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel  @Inject constructor(
    val searchFriendsUseCase: SearchFriendsUseCase,
    val addFriendUseCase: AddFriendUseCase
): BaseViewModel() {

    private val _friendListState = MutableStateFlow<FriendListResponse?>(null)
    val friendListState = _friendListState.asStateFlow()

    private val _friendAddedState = MutableStateFlow<FriendResponse?>(null)
    val friendAddedState = _friendAddedState.asStateFlow()

    fun searchNewFriends(name : String) = launch {
        _friendListState.value = searchFriendsUseCase.invoke(name)
    }

    fun addFriend(id:String) = launch {
        _friendAddedState.value = addFriendUseCase.invoke(id)
    }
}