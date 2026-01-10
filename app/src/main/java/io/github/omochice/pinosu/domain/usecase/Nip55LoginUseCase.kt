package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import javax.inject.Inject

/**
 * NIP-55 based LoginUseCase implementation
 *
 * @property authRepository Authentication repository
 */
class Nip55LoginUseCase @Inject constructor(private val authRepository: AuthRepository) :
    LoginUseCase {

  /**
   * Check if the NIP-55 signer app is installed
   *
   * @return true if NIP-55 signer is installed
   */
  override fun checkNip55SignerInstalled(): Boolean {
    return authRepository.checkNip55SignerInstalled()
  }
}
