package com.gf.apkcarrera.features.f2_friends.viewmodel

import com.gf.apkcarrera.features.f2_friends.usecase.AcceptFriendRequestUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.CancelFriendRequestUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.GetFriendsRequestsUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.GetFriendsUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.IgnoreFriendRequestUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.SearchFriendsUseCase
import com.gf.apkcarrera.features.f2_friends.usecase.SendFriendRequestUseCase
import com.gf.common.platform.BaseViewModel
import com.gf.common.response.FriendListResponse
import com.gf.common.response.FriendResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    val getFriendsUseCase: GetFriendsUseCase
): BaseViewModel() {

    private val _friendListState = MutableStateFlow<FriendListResponse?>(null)
    val friendListState = _friendListState.asStateFlow()

    private val _friendActionOkState = MutableStateFlow<FriendResponse?>(null)
    val friendActionOkState = _friendActionOkState.asStateFlow()

    private val _friendActionCancelState = MutableStateFlow<FriendResponse?>(null)
    val friendActionCancelState = _friendActionCancelState.asStateFlow()

    // 1. Amigos LISTA AMIGOS
    fun searchFriends() = launch {
        _friendListState.value = getFriendsUseCase.invoke()
    }

    // 2. Peticiones LISTA AMIGOS
    fun searchFriendRequests() = launch {
        _friendListState.value = getFriendsRequestsUseCase.invoke()
    }

    // 2. Peticiones ACEPTAR
    fun acceptFriendRequest(id:String) = launch {
        _friendActionOkState.value = acceptFriendRequestUseCase.invoke(id)
    }

    // 2. Peticiones ACEPTAR
    fun ignoreFriendRequest(id:String) = launch {
        _friendActionCancelState.value = ignoreFriendRequestUseCase.invoke(id)
    }

    // 3. Añadir LISTA AMIGOS BUSCADOR
    fun searchNewFriends(name : String) = launch {
        _friendListState.value = searchFriendsUseCase.invoke(name)
    }

    // 3. Añadir ENVIAR PETICIÓN
    fun sendFriendRequest(id:String) = launch {
        _friendActionOkState.value = sendFriendRequestUseCase.invoke(id)
    }

    // 3. Añadir CANCELAR PETICIÓN
    fun cancelFriendRequest(id:String) = launch {
        _friendActionCancelState.value = cancelFriendRequestUseCase.invoke(id)
    }
}