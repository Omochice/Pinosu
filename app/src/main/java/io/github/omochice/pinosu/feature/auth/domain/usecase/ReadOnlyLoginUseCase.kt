package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.model.error.LoginError
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase for read-only login via npub public key entry.
 *
 * Parses the npub, creates a User, and saves with [LoginMode.ReadOnly].
 */
interface ReadOnlyLoginUseCase {
  /**
   * Attempt read-only login with the given npub.
   *
   * @param npub Nostr public key in npub format
   * @return Success(User) on success, Failure(LoginError) on failure
   */
  suspend operator fun invoke(npub: String): Result<User>
}

/**
 * Default [ReadOnlyLoginUseCase] implementation
 *
 * @param authRepository Repository for authentication state management
 */
class ReadOnlyLoginUseCaseImpl @Inject constructor(private val authRepository: AuthRepository) :
    ReadOnlyLoginUseCase {

  override suspend fun invoke(npub: String): Result<User> {
    val pubkey = Pubkey.parse(npub) ?: return Result.failure(LoginError.InvalidPubkey)
    val user = User(pubkey)

    return authRepository
        .saveLoginState(user, LoginMode.ReadOnly)
        .fold(
            onSuccess = { Result.success(user) },
            onFailure = { Result.failure(LoginError.UnknownError(it)) })
  }
}
