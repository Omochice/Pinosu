package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.model.UnsignedNostrEvent
import io.github.omochice.pinosu.data.repository.BookmarkRepository
import javax.inject.Inject

/**
 * Implementation of PostBookmarkUseCase
 *
 * Delegates to BookmarkRepository to create and publish bookmark events.
 *
 * @property bookmarkRepository Repository for bookmark data access
 */
class PostBookmarkUseCaseImpl
@Inject
constructor(private val bookmarkRepository: BookmarkRepository) : PostBookmarkUseCase {

  override suspend fun createUnsignedEvent(
      pubkey: String,
      url: String,
      title: String?,
      categories: List<String>,
      comment: String
  ): Result<UnsignedNostrEvent> {
    if (url.isBlank()) {
      return Result.failure(IllegalArgumentException("URL cannot be blank"))
    }
    return bookmarkRepository.createBookmarkEvent(pubkey, url, title, categories, comment)
  }

  override suspend fun publishSignedEvent(signedEventJson: String): Result<Unit> {
    return bookmarkRepository.publishBookmark(signedEventJson)
  }
}
