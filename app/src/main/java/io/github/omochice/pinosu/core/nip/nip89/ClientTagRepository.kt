package io.github.omochice.pinosu.core.nip.nip89

import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for NIP-89 client tag settings.
 *
 * Provides access to the client tag enabled preference with reactive updates.
 */
interface ClientTagRepository {

  /** Observable StateFlow of client tag enabled preference */
  val clientTagEnabledFlow: StateFlow<Boolean>

  /**
   * Save client tag enabled preference.
   *
   * @param enabled Whether to include client tag in published events
   */
  fun setClientTagEnabled(enabled: Boolean)
}
