package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Implementation of SetBootstrapRelaysUseCase.
 *
 * Delegates to SettingsRepository for bootstrap relay persistence.
 */
class SetBootstrapRelaysUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : SetBootstrapRelaysUseCase {

  override fun invoke(relays: Set<String>) {
    settingsRepository.setBootstrapRelays(relays)
  }
}
