package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.core.logout.LogoutCleanup
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * NIP-55 based [LogoutUseCase] implementation
 *
 * @param authRepository Repository for authentication operations
 * @param cleanups Deletion of the data the features keep for the user, run once the user is logged
 *   out
 */
class Nip55LogoutUseCase
@Inject
constructor(
    private val authRepository: AuthRepository,
    private val cleanups: Set<@JvmSuppressWildcards LogoutCleanup>,
) : LogoutUseCase {

  /**
   * Execute logout process
   *
   * @return Success on success, Failure(LogoutError) on failure
   */
  override suspend fun invoke(): Result<Unit> =
      authRepository.logout().onSuccess { cleanups.forEach { it.cleanUp() } }
}
