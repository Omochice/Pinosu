package io.github.omochice.pinosu.feature.bookmark.domain.usecase

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkList

/**
 * UseCase interface for retrieving bookmark list
 *
 * Fetches kind:39701 bookmark events from Nostr relays for a given user.
 */
interface GetBookmarkListUseCase {
  /**
   * Retrieve a bookmark list, optionally constrained to a single author
   *
   * @param authorPubkey Nostr public key (Bech32-encoded format, starts with npub1) to constrain
   *   the query to a single author (Local tab), or null to query all authors (Global tab)
   * @param until Unix timestamp upper bound for pagination (inclusive per NIP-01), null for latest
   * @return Success(BookmarkList) if found, Success(null) if no bookmarks, Failure on error
   */
  suspend operator fun invoke(authorPubkey: String?, until: Long? = null): Result<BookmarkList?>
}
