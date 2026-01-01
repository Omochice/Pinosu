package io.github.omochice.pinosu.domain.usecase

/**
 * Logout process UseCase interface
 *
 * - Delegation to AuthRepository
 * - Guarantee of idempotency
 *
 * Requirements: 2.4, 2.5
 */
interface LogoutUseCase {

  /**
   * Execute logout process
   *
   * Delegates to AuthRepository to clear login state from local storage. Guarantees idempotency and
   * processes normally even if already logged out.
   *
   *
   * @return Success on success, Failure(LogoutError) on failure
   */
  suspend operator fun invoke(): Result<Unit>
}
