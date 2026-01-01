package io.github.omochice.pinosu.domain.usecase

/**
 * Login process UseCase interface
 * - Amber installation verification
 * - Delegation to AuthRepository
 */
interface LoginUseCase {

  /**
   * Check if the Amber app is installed
   *
   * Delegates to AuthRepository to verify Amber installation status.
   *
   * @return true if Amber is installed
   */
  fun checkAmberInstalled(): Boolean
}
