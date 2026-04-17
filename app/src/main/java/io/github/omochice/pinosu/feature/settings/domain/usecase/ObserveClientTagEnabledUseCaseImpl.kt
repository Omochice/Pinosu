package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementation of ObserveClientTagEnabledUseCase.
 *
 * Provides reactive observation of client tag enabled preference changes via StateFlow.
 */
class ObserveClientTagEnabledUseCaseImpl
@Inject
constructor(private val clientTagRepository: ClientTagRepository) : ObserveClientTagEnabledUseCase {

  override fun invoke(): StateFlow<Boolean> = clientTagRepository.clientTagEnabledFlow
}
