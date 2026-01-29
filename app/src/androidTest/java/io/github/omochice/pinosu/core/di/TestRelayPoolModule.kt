package io.github.omochice.pinosu.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.github.omochice.pinosu.core.relay.FakeRelayPool
import io.github.omochice.pinosu.core.relay.RelayPool

/**
 * Test module that replaces RelayPoolModule with FakeRelayPool for instrumented tests.
 *
 * Uses @TestInstallIn to automatically replace the production RelayPoolModule in
 * all @HiltAndroidTest classes.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RelayPoolModule::class])
object TestRelayPoolModule {

  @Provides fun provideRelayPool(): RelayPool = FakeRelayPool()
}
