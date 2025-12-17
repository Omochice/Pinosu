package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.domain.model.User

/**
 * Login state retrieval UseCase interface
 *
 * Task 6.3: GetLoginStateUseCase implementation
 * - Delegation to AuthRepository
 * - Guarantee of read-only operation
 *
 * Requirements: 2.2, 2.3
 */
interface GetLoginStateUseCase {

  /**
   * Get the login state
   *
   * Delegates to AuthRepository to retrieve user information from local storage. This is a
   * read-only operation and does not change the login state.
   *
   * Task 6.3: invoke() implementation Requirement 2.2, 2.3: Login state verification and main
   * screen display
   *
   * @return User if logged in, null if not logged in
   */
  suspend operator fun invoke(): User?
}
