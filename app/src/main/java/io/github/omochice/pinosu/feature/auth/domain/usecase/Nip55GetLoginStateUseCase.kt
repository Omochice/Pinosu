package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * NIP-55 based [GetLoginStateUseCase] implementation
 *
 * @param authRepository Repository for authentication operations
 */
class Nip55GetLoginStateUseCase @Inject constructor(private val authRepository: AuthRepository) :
    GetLoginStateUseCase {

  /**
   * Get the login state
   *
   * @return User if logged in, null if not logged in
   */
  override suspend fun invoke(): User? = authRepository.getLoginState()
}
