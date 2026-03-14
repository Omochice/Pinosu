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
 * Uses user-configured relays when available, otherwise falls back to default bootstrap relays.
 */
@Singleton
class SettingsBootstrapRelayProvider
@Inject
constructor(private val localSettingsDataSource: LocalSettingsDataSource) : BootstrapRelayProvider {

  override fun getBootstrapRelays(): List<RelayConfig> {
    val urls =
        localSettingsDataSource.getBootstrapRelays()
            ?: Nip65RelayListFetcherImpl.DEFAULT_BOOTSTRAP_RELAY_URLS.toSet()
    return urls.map { url -> RelayConfig(url = url) }
  }
}
