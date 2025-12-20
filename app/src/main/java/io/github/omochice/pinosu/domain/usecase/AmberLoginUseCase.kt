package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Amber-based LoginUseCase implementation
 *
 * Delegates to AuthRepository to execute login process.
 *
 * Task 6.1: LoginUseCase implementation Requirements: 1.1, 1.3, 1.4, 1.5, 4.5
 *
 * @property authRepository Authentication repository
 */
class AmberLoginUseCase @Inject constructor(private val authRepository: AuthRepository) :
    LoginUseCase {

  /**
   * Check if the Amber app is installed
   *
   * Delegates to AuthRepository to verify Amber installation status.
   *
   * Task 6.1: checkAmberInstalled() implementation Requirement 1.2: Amber not installed detection
   *
   * @return true if Amber is installed
   */
  override fun checkAmberInstalled(): Boolean {
    return authRepository.checkAmberInstalled()
  }
}
