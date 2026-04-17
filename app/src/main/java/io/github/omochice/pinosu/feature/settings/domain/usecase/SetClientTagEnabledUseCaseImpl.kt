package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import javax.inject.Inject

/**
 * Implementation of SetClientTagEnabledUseCase.
 *
 * Delegates to ClientTagRepository for client tag enabled persistence.
 */
class SetClientTagEnabledUseCaseImpl
@Inject
constructor(private val clientTagRepository: ClientTagRepository) : SetClientTagEnabledUseCase {

  override fun invoke(enabled: Boolean) {
    clientTagRepository.setClientTagEnabled(enabled)
  }
}
