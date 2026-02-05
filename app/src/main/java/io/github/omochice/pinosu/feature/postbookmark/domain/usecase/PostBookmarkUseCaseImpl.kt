package io.github.omochice.pinosu.feature.postbookmark.domain.usecase

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.bookmark.data.repository.BookmarkRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [PostBookmarkUseCase]
 *
 * @param bookmarkRepository Repository for bookmark operations
 * @param getLoginStateUseCase UseCase for retrieving current login state
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
        user.pubkey.hex ?: return Result.failure(IllegalArgumentException("Invalid npub format"))

    return Result.success(
        bookmarkRepository.createBookmarkEvent(
            hexPubkey = hexPubkey,
            url = url,
            title = title,
            categories = categories,
            comment = comment))
  }

  override suspend fun publishSignedEvent(signedEventJson: String): Result<PublishResult> =
      bookmarkRepository.publishBookmark(signedEventJson)
}
