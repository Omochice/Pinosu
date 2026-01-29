package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.core.util.Bech32
import io.github.omochice.pinosu.data.repository.BookmarkRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PostBookmarkUseCase
 *
 * @property bookmarkRepository Repository for bookmark operations
 * @property getLoginStateUseCase Use case for getting current user
 */
@Singleton
class PostBookmarkUseCaseImpl
@Inject
constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val getLoginStateUseCase: GetLoginStateUseCase
) : PostBookmarkUseCase {

  override suspend fun createUnsignedEvent(
      url: String,
      title: String,
      categories: List<String>,
      comment: String
  ): Result<UnsignedNostrEvent> {
    val user =
        getLoginStateUseCase() ?: return Result.failure(IllegalStateException("User not logged in"))

    val hexPubkey =
        Bech32.npubToHex(user.pubkey)
            ?: return Result.failure(IllegalArgumentException("Invalid npub format"))

    return Result.success(
        bookmarkRepository.createBookmarkEvent(
            hexPubkey = hexPubkey,
            url = url,
            title = title,
            categories = categories,
            comment = comment))
  }

  override suspend fun publishSignedEvent(signedEventJson: String): Result<PublishResult> {
    return bookmarkRepository.publishBookmark(signedEventJson)
  }
}
