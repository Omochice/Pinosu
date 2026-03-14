package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementation of ObserveBootstrapRelaysUseCase.
 *
 * Provides reactive observation of bootstrap relay preference changes via StateFlow.
 */
class ObserveBootstrapRelaysUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : ObserveBootstrapRelaysUseCase {

  override fun invoke(): StateFlow<Set<String>> = settingsRepository.bootstrapRelaysFlow
}
