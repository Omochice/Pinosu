package io.github.omochice.pinosu.feature.auth.domain.usecase

/**
 * Login process UseCase interface
 * - NIP-55 signer installation verification
 * - Delegation to AuthRepository
 */
interface LoginUseCase {

  /**
   * Check if the NIP-55 signer app is installed
   *
   * Delegates to AuthRepository to verify NIP-55 signer installation status.
   *
   * @return true if NIP-55 signer is installed
   */
  fun checkNip55SignerInstalled(): Boolean
}
