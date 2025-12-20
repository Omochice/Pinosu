package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.User
import javax.inject.Inject

/**
 * Amber-based GetLoginStateUseCase implementation class
 *
 * Task 6.3: GetLoginStateUseCase implementation
 * - Delegate to AuthRepository to retrieve login state
 * - Read-only operation
 *
 * Requirements: 2.2, 2.3
 *
 * @property authRepository Authentication repository
 */
class AmberGetLoginStateUseCase @Inject constructor(private val authRepository: AuthRepository) :
    GetLoginStateUseCase {

  /**
   * Get the login state
   *
   * Delegates to AuthRepository to retrieve user information from local storage. This operation is
   * read-only and does not change state.
   *
   * Task 6.3: invoke() implementation Requirement 2.2, 2.3: Login state verification and main
   * screen display
   *
   * @return User if logged in, null if not logged in
   */
  override suspend fun invoke(): User? {
    return authRepository.getLoginState()
  }
}
