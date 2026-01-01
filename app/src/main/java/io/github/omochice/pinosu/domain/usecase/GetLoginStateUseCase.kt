package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.domain.model.User

/**
 * Login state retrieval UseCase interface
 * - Delegation to AuthRepository
 * - Guarantee of read-only operation
 */
interface GetLoginStateUseCase {

  /**
   * Get the login state
   *
   * Delegates to AuthRepository to retrieve user information from local storage. This is a
   * read-only operation and does not change the login state.
   *
   * screen display
   *
   * @return User if logged in, null if not logged in
   */
  suspend operator fun invoke(): User?
}
