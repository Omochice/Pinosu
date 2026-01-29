package io.github.omochice.pinosu.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for UseCase dependency injection
 *
 * This module is now empty as use cases have been moved to feature modules. It remains for
 * potential future cross-feature use cases.
 */
@Suppress("AnnotationOnSeparateLine")
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule
