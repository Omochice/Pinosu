package io.github.omochice.pinosu.feature.feed.di

import dagger.Binds
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
interface FeedModule {

  @Binds
  fun bindSubscriptionRepository(impl: RelayFeedSubscriptionRepository): FeedSubscriptionRepository

  @Binds fun bindFeedParser(impl: JsoupFeedParser): FeedParser

  @Binds fun bindFeedRepository(impl: HttpFeedRepository): FeedRepository

  @Binds fun bindFeedDiscoveryRepository(impl: HttpFeedDiscoveryRepository): FeedDiscoveryRepository

  @Binds fun bindArticleImageRepository(impl: OgpArticleImageRepository): ArticleImageRepository

  @Binds fun bindLoadFeedTimelineUseCase(impl: LoadFeedTimelineUseCaseImpl): LoadFeedTimelineUseCase

  @Binds
  fun bindLoadListUseCase(impl: LoadFeedSubscriptionsUseCaseImpl): LoadFeedSubscriptionsUseCase

  @Binds
  fun bindPrepareChangeUseCase(
      impl: PrepareFeedSubscriptionChangeUseCaseImpl
  ): PrepareFeedSubscriptionChangeUseCase

  @Binds
  fun bindPublishListUseCase(
      impl: PublishFeedSubscriptionListUseCaseImpl
  ): PublishFeedSubscriptionListUseCase

  companion object {
    /**
     * Declared with `@Provides` rather than `@Binds`, because ktfmt joins `@Binds @IntoSet` and the
     * function onto one line, which detekt's AnnotationOnSeparateLine rule rejects.
     */
    @Provides
    @IntoSet
    fun provideFeedLogoutCleanup(cleanup: FeedLogoutCleanup): LogoutCleanup = cleanup
  }
}
