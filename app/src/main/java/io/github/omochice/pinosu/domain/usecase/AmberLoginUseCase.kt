package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Amber-based LoginUseCase implementation
 *
 * @property authRepository Authentication repository
 */
class AmberLoginUseCase @Inject constructor(private val authRepository: AuthRepository) :
    LoginUseCase {

  /**
   * Check if the Amber app is installed
   *
   * @return true if Amber is installed
   */
  override fun checkAmberInstalled(): Boolean {
    return authRepository.checkAmberInstalled()
  }
}
