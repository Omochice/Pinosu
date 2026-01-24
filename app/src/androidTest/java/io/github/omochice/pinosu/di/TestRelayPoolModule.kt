package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.github.omochice.pinosu.data.relay.FakeRelayPool
import io.github.omochice.pinosu.data.relay.RelayPool
import javax.inject.Singleton

/**
 * Test module that replaces RelayPoolModule with FakeRelayPool for instrumented tests
 *
 * Uses @TestInstallIn to automatically replace the production RelayPoolModule in all
 *
 * @HiltAndroidTest classes.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RelayPoolModule::class])
object TestRelayPoolModule {

  private val fakeRelayPool = FakeRelayPool()

  @Provides @Singleton fun provideRelayPool(): RelayPool = fakeRelayPool

  /** Provides access to the FakeRelayPool for test setup and verification */
  fun getFake(): FakeRelayPool = fakeRelayPool
}
