package io.github.omochice.pinosu.feature.shareintent.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.shareintent.domain.usecase.ExtractSharedContentUseCase
import io.github.omochice.pinosu.feature.shareintent.domain.usecase.ExtractSharedContentUseCaseImpl

/**
 * Hilt module for share intent feature dependency injection
 *
 * Provides UseCase dependencies for extracting shared content from incoming intents.
 */
@Module
@InstallIn(SingletonComponent::class)
interface ShareIntentModule {

  @Binds
  fun bindExtractSharedContent(impl: ExtractSharedContentUseCaseImpl): ExtractSharedContentUseCase
}
