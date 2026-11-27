package io.github.omochice.pinosu.feature.feed.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.github.omochice.pinosu.core.logout.LogoutCleanup
import io.github.omochice.pinosu.feature.feed.data.local.FeedLogoutCleanup
import io.github.omochice.pinosu.feature.feed.data.parser.FeedParser
import io.github.omochice.pinosu.feature.feed.data.parser.JsoupFeedParser
import io.github.omochice.pinosu.feature.feed.data.repository.HttpFeedDiscoveryRepository
import io.github.omochice.pinosu.feature.feed.data.repository.HttpFeedRepository
import io.github.omochice.pinosu.feature.feed.data.repository.OgpArticleImageRepository
import io.github.omochice.pinosu.feature.feed.data.repository.RelayFeedSubscriptionRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.ArticleImageRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedDiscoveryRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedSubscriptionsUseCase
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedSubscriptionsUseCaseImpl
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedTimelineUseCase
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedTimelineUseCaseImpl
import io.github.omochice.pinosu.feature.feed.domain.usecase.PrepareFeedSubscriptionChangeUseCase
import io.github.omochice.pinosu.feature.feed.domain.usecase.PrepareFeedSubscriptionChangeUseCaseImpl
import io.github.omochice.pinosu.feature.feed.domain.usecase.PublishFeedSubscriptionListUseCase
import io.github.omochice.pinosu.feature.feed.domain.usecase.PublishFeedSubscriptionListUseCaseImpl

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
  fun provideArticleImageRepository(impl: OgpArticleImageRepository): ArticleImageRepository = impl

  @Provides
  @IntoSet
  fun provideFeedLogoutCleanup(cleanup: FeedLogoutCleanup): LogoutCleanup = cleanup

  @Provides
  fun provideLoadFeedTimelineUseCase(impl: LoadFeedTimelineUseCaseImpl): LoadFeedTimelineUseCase =
      impl

  @Provides
  fun provideLoadListUseCase(impl: LoadFeedSubscriptionsUseCaseImpl): LoadFeedSubscriptionsUseCase =
      impl

  @Provides
  fun providePrepareChangeUseCase(
      impl: PrepareFeedSubscriptionChangeUseCaseImpl
  ): PrepareFeedSubscriptionChangeUseCase = impl

  @Provides
  fun providePublishListUseCase(
      impl: PublishFeedSubscriptionListUseCaseImpl
  ): PublishFeedSubscriptionListUseCase = impl
}
