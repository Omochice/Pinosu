package io.github.omochice.pinosu.data.repository

import io.github.omochice.pinosu.domain.model.BookmarkList

/**
 * Repository interface for bookmark data access
 *
 * Provides access to Nostr kind:39701 bookmark events from relays.
 */
interface BookmarkRepository {
  /**
   * Retrieve bookmark list for the specified public key
   *
   * @param pubkey Nostr public key (Bech32-encoded format, starts with npub1)
   * @return Success(BookmarkList) if found, Success(null) if no bookmarks, Failure on error
   */
  suspend fun getBookmarkList(pubkey: String): Result<BookmarkList?>
}
