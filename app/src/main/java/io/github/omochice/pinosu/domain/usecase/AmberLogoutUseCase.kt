package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Amber-based LogoutUseCase implementation
 *
 * @property authRepository Authentication repository
 */
class AmberLogoutUseCase @Inject constructor(private val authRepository: AuthRepository) :
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
