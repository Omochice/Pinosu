package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import javax.inject.Inject

/**
 * LogoutUseCase implementation
 *
 * Delegates to AuthRepository to execute logout process. Guarantees idempotency and functions
 * normally even when called multiple times.
 *
 * Task 6.2: LogoutUseCase implementation Requirements: 2.4, 2.5
 *
 * @property authRepository Authentication repository
 */
class LogoutUseCaseImpl @Inject constructor(private val authRepository: AuthRepository) :
    LogoutUseCase {

  /**
   * Execute logout process
   *
   * Delegates to AuthRepository to clear login state from local storage. Since
   * AuthRepository.logout() guarantees idempotency, this UseCase inherits idempotency.
   *
   * Task 6.2: invoke() implementation Requirement 2.4, 2.5: Logout functionality and idempotency
   *
   * @return Success on success, Failure(LogoutError) on failure
   */
  override suspend fun invoke(): Result<Unit> {
    return authRepository.logout()
  }
}
