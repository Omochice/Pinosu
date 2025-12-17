package io.github.omochice.pinosu.domain.usecase

/**
 * Logout process UseCase interface
 *
 * Task 6.2: LogoutUseCase implementation
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
   * Task 6.2: invoke() implementation Requirement 2.4, 2.5: Logout functionality and idempotency
   *
   * @return Success on success, Failure(LogoutError) on failure
   */
  suspend operator fun invoke(): Result<Unit>
}
