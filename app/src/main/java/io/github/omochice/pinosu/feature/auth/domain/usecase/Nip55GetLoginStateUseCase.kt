package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.feature.auth.data.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.model.User
import javax.inject.Inject

/**
 * NIP-55 based GetLoginStateUseCase implementation class
 *
 * @property authRepository Authentication repository
 */
class Nip55GetLoginStateUseCase @Inject constructor(private val authRepository: AuthRepository) :
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
