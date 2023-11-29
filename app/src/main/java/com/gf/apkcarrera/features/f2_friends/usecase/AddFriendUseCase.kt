package com.gf.apkcarrera.features.f2_friends.usecase

import com.gf.apkcarrera.features.f2_friends.repository.FriendsRepository
import com.gf.common.response.FriendResponse
import com.gf.common.response.GenericResponse
import javax.inject.Inject

class AddFriendUseCase @Inject constructor(private val friendsRepository: FriendsRepository) {
    suspend operator fun invoke(name : String) : FriendResponse = friendsRepository.addFriend(name)
}