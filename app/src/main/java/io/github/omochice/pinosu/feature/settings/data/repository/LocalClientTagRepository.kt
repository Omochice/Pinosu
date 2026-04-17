package io.github.omochice.pinosu.feature.settings.data.repository

import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import io.github.omochice.pinosu.feature.settings.data.local.LocalSettingsDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

/**
 * Local implementation of [ClientTagRepository].
 *
 * Delegates to [LocalSettingsDataSource] for client tag preference persistence.
 */
@Singleton
class LocalClientTagRepository
@Inject
constructor(private val localSettingsDataSource: LocalSettingsDataSource) : ClientTagRepository {

  override val clientTagEnabledFlow: StateFlow<Boolean> =
      localSettingsDataSource.clientTagEnabledFlow

  override fun setClientTagEnabled(enabled: Boolean) {
    localSettingsDataSource.setClientTagEnabled(enabled)
  }
}
