package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.User
import javax.inject.Inject

/**
 * Amber-based GetLoginStateUseCase implementation class
 *
 * @property authRepository Authentication repository
 */
class AmberGetLoginStateUseCase @Inject constructor(private val authRepository: AuthRepository) :
    GetLoginStateUseCase {

  /**
   * Get the login state
   *
   * @return User if logged in, null if not logged in
   */
  override suspend fun invoke(): User? {
    return authRepository.getLoginState()
  }
}
