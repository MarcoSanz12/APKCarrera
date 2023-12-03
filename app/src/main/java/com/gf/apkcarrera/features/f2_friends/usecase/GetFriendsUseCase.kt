package com.gf.apkcarrera.features.f2_friends.usecase

import com.gf.apkcarrera.features.f2_friends.repository.FriendsRepository
import com.gf.common.response.FriendListResponse
import javax.inject.Inject

class GetFriendsUseCase @Inject constructor(private val friendsRepository: FriendsRepository) {
    suspend operator fun invoke() : FriendListResponse = friendsRepository.getFriends()
}