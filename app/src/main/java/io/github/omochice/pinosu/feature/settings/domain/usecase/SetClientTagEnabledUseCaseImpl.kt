package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Implementation of SetClientTagEnabledUseCase.
 *
 * Delegates to SettingsRepository for client tag enabled persistence.
 */
class SetClientTagEnabledUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : SetClientTagEnabledUseCase {

  override fun invoke(enabled: Boolean) {
    settingsRepository.setClientTagEnabled(enabled)
  }
}
