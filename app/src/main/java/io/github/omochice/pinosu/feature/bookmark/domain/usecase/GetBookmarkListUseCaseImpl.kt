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
   * Retrieve a bookmark list, optionally constrained to a single author
   *
   * @param authorPubkey Nostr public key (Bech32-encoded format, starts with npub1) to constrain
   *   the query to a single author (Local tab), or null to query all authors (Global tab)
   * @param until Unix timestamp upper bound for pagination (inclusive per NIP-01), null for latest
   * @return Success(BookmarkList) if found, Success(null) if no bookmarks, Failure on error
   */
  override suspend fun invoke(authorPubkey: String?, until: Long?): Result<BookmarkList?> =
      bookmarkRepository.getBookmarkList(authorPubkey, until)
}
