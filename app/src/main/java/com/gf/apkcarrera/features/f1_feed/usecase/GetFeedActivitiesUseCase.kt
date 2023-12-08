package com.gf.apkcarrera.features.f1_feed.usecase

import com.gf.apkcarrera.features.f1_feed.repository.FeedRepository
import com.gf.apkcarrera.features.f5_profile.repository.ProfileRepository
import com.gf.common.response.FeedResponse
import com.gf.common.response.ProfileResponse
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class GetFeedActivitiesUseCase @Inject constructor(private val feedRepository: FeedRepository) {
    suspend operator fun invoke(scope: CoroutineScope) : FeedResponse = feedRepository.getFeedActivities(scope)
}