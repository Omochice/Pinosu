package io.github.omochice.pinosu.domain.usecase

/**
 * Login process UseCase interface
 *
 * Task 6.1: LoginUseCase implementation
 * - Amber installation verification
 * - Delegation to AuthRepository
 *
 * Requirements: 1.1, 1.3, 1.4, 1.5, 4.5
 */
interface LoginUseCase {

  /**
   * Check if the Amber app is installed
   *
   * Delegates to AuthRepository to verify Amber installation status.
   *
   * Task 6.1: checkAmberInstalled() implementation Requirement 1.2: Amber not installed detection
   *
   * @return true if Amber is installed
   */
  fun checkAmberInstalled(): Boolean
}
