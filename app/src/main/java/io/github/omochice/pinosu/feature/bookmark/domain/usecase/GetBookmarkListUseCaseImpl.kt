package io.github.omochice.pinosu.feature.bookmark.domain.usecase

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkList
import io.github.omochice.pinosu.feature.bookmark.domain.repository.BookmarkRepository
import javax.inject.Inject

/**
 * Implementation of [GetBookmarkListUseCase]
 *
 * Delegates to [BookmarkRepository] to fetch bookmark events from relays.
 *
 * @param bookmarkRepository Repository for bookmark operations
 */
class GetBookmarkListUseCaseImpl
@Inject
constructor(private val bookmarkRepository: BookmarkRepository) : GetBookmarkListUseCase {

  /**
   * Retrieve bookmark list for the specified public key
   *
   * @param pubkey Nostr public key (Bech32-encoded format, starts with npub1)
   * @return Success(BookmarkList) if found, Success(null) if no bookmarks, Failure on error
   */
  override suspend fun invoke(pubkey: String): Result<BookmarkList?> =
      bookmarkRepository.getBookmarkList(pubkey)
}
