package com.gf.apkcarrera.features.f2_friends.viewmodel

import android.util.Log
import com.gf.apkcarrera.features.f2_friends.usecase.AcceptFriendRequestUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.CancelFriendRequestUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.GetFriendsRequestsUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.GetFriendsUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.IgnoreFriendRequestUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.RemoveFriendUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.SearchFriendsUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.SendFriendRequestUseCase
import com.gf.common.entity.friend.FriendModel
import com.gf.common.entity.friend.FriendStatus
import com.gf.common.platform.BaseViewModel
import com.gf.common.response.FriendListResponse
import com.gf.common.response.FriendResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel  @Inject constructor(
    val searchFriendsUseCase: SearchFriendsUseCase,
    val sendFriendRequestUseCase: SendFriendRequestUseCase,
    val cancelFriendRequestUseCase: CancelFriendRequestUseCase,
    val getFriendsRequestsUseCase: GetFriendsRequestsUseCase,
    val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    val ignoreFriendRequestUseCase: IgnoreFriendRequestUseCase,
    val getFriendsUseCase: GetFriendsUseCase,
    val removeFriendUseCase: RemoveFriendUseCase
): BaseViewModel() {
    
    companion object{
        private const val TAG = "FriendsViewModel"
    }

    // 1. FriendsListFragment
    private val _friendListState = MutableStateFlow<FriendListResponse?>(null)
    val friendListState = _friendListState.asStateFlow()

    private val _friendRemoveState = MutableSharedFlow<FriendResponse?>()
    val friendRemoveState = _friendRemoveState.asSharedFlow()

    // 2. FriendsRequestsFragment
    private val _friendRequestsListState = MutableStateFlow<FriendListResponse?>(null)
    val friendRequestsListState = _friendRequestsListState.asStateFlow()

    private val _friendRequestOkState = MutableSharedFlow<FriendResponse?>()
    val friendRequestOkState = _friendRequestOkState.asSharedFlow()

    private val _friendRequestIgnoreState = MutableSharedFlow<FriendResponse?>()
    val friendRequestIgnoreState = _friendRequestIgnoreState.asSharedFlow()

    // 3. FriendsAddFragment
    private val _friendAddListState = MutableSharedFlow<FriendListResponse?>(0)
    val friendAddListState = _friendAddListState.asSharedFlow()

    private val _friendAddState = MutableSharedFlow<FriendResponse?>()
    val friendAddState = _friendAddState.asSharedFlow()

    private val _friendCancelState = MutableSharedFlow<FriendResponse?>()
    val friendCancelState = _friendCancelState.asSharedFlow()

    // 1. Amigos LISTA AMIGOS
    fun getFriends() = launch {
        _friendListState.emit(getFriendsUseCase.invoke())
    }

    // 1. Amigos ELIMINAR AMIGO
    fun removeFriend(id:String) = launch {
        val response = removeFriendUseCase(id)
        _friendRemoveState.emit(response)
    }
    // 2. Peticiones LISTA AMIGOS
    fun searchFriendRequests() = launch {
        _friendRequestsListState.emit(getFriendsRequestsUseCase.invoke())
    }

    fun friendAccepted(friend: FriendModel) = launch {
        val friendList = if (friendListState.value is FriendListResponse.Succesful)
            (friendListState.value as FriendListResponse.Succesful).friendList.toMutableList()
        else
            mutableListOf()

        Log.d(TAG, "friendAccepted: Añadiendo amigo")
        friend.friendStatus = FriendStatus.FRIEND
        _friendListState.value = FriendListResponse.Succesful(friendList.apply { add(friend) })
    }
    // 2. Peticiones ACEPTAR
    fun acceptFriendRequest(id:String) = launch {
        val response = acceptFriendRequestUseCase.invoke(id)
        _friendRequestOkState.emit(response)
    }

    // 2. Peticiones ACEPTAR
    fun ignoreFriendRequest(id:String) = launch {
        val response = ignoreFriendRequestUseCase.invoke(id)
        _friendRequestIgnoreState.emit(response)

    }

    // 3. Añadir LISTA AMIGOS BUSCADOR
    fun searchNewFriends(name : String) = launch {
        _friendAddListState.emit(searchFriendsUseCase.invoke(name))
    }

    // 3. Añadir ENVIAR PETICIÓN
    fun sendFriendRequest(id:String) = launch {
        val response = sendFriendRequestUseCase.invoke(id)
        _friendAddState.emit(response)
    }

    // 3. Añadir CANCELAR PETICIÓN
    fun cancelFriendRequest(id:String) = launch {
        val response = cancelFriendRequestUseCase.invoke(id)
        _friendCancelState.emit(response)
    }
}