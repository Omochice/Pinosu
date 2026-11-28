package io.github.omochice.pinosu.feature.feed.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.feed.data.parser.FeedParser
import io.github.omochice.pinosu.feature.feed.data.parser.JsoupFeedParser
import io.github.omochice.pinosu.feature.feed.data.repository.HttpFeedDiscoveryRepository
import io.github.omochice.pinosu.feature.feed.data.repository.HttpFeedRepository
import io.github.omochice.pinosu.feature.feed.data.repository.RelayFeedSubscriptionRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedDiscoveryRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedTimelineUseCase
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedTimelineUseCaseImpl

/** Hilt module for the feed feature */
@Module
@InstallIn(SingletonComponent::class)
object FeedModule {

  @Provides
  fun provideSubscriptionRepository(
      impl: RelayFeedSubscriptionRepository
  ): FeedSubscriptionRepository = impl

  @Provides fun provideFeedParser(impl: JsoupFeedParser): FeedParser = impl

  @Provides fun provideFeedRepository(impl: HttpFeedRepository): FeedRepository = impl

  @Provides
  fun provideFeedDiscoveryRepository(impl: HttpFeedDiscoveryRepository): FeedDiscoveryRepository =
      impl

  @Provides
  fun provideLoadFeedTimelineUseCase(impl: LoadFeedTimelineUseCaseImpl): LoadFeedTimelineUseCase =
      impl
}
