package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.feature.auth.data.repository.AuthRepository
import javax.inject.Inject

/**
 * NIP-55 based LogoutUseCase implementation
 *
 * @property authRepository Authentication repository
 */
class Nip55LogoutUseCase @Inject constructor(private val authRepository: AuthRepository) :
    LogoutUseCase {

  /**
   * Execute logout process
   *
   * @return Success on success, Failure(LogoutError) on failure
   */
  override suspend fun invoke(): Result<Unit> {
    return authRepository.logout()
  }
}
