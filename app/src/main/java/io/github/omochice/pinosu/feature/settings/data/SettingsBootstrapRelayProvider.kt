package io.github.omochice.pinosu.feature.settings.data

import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcherImpl
import io.github.omochice.pinosu.core.relay.BootstrapRelayProvider
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.feature.settings.data.local.LocalSettingsDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [BootstrapRelayProvider] implementation backed by user settings.
 *
 * Always includes the default bootstrap relay and appends any user-configured additional relays.
 */
@Singleton
class SettingsBootstrapRelayProvider
@Inject
constructor(private val localSettingsDataSource: LocalSettingsDataSource) : BootstrapRelayProvider {

  override fun getBootstrapRelays(): List<RelayConfig> {
    val defaultRelay = RelayConfig(url = Nip65RelayListFetcherImpl.BOOTSTRAP_RELAY_URL)
    val userRelays =
        localSettingsDataSource.getBootstrapRelays().map { url -> RelayConfig(url = url) }
    return listOf(defaultRelay) + userRelays
  }
}
