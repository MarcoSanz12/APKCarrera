package com.gf.apkcarrera.features.f1_feed.usecase

import com.gf.apkcarrera.features.f1_feed.repository.FeedRepository
import com.gf.common.response.FeedResponse
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class GetFeedActivitiesUseCase @Inject constructor(private val feedRepository: FeedRepository) {
    suspend operator fun invoke(userId : String?, scope: CoroutineScope) : FeedResponse = feedRepository.getFeedActivities(userId,scope)
}