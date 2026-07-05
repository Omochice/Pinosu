package io.github.omochice.pinosu.feature.bookmark.domain.repository

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkList

/**
 * Repository interface for bookmark data access
 *
 * Provides access to Nostr kind:39701 bookmark events from relays.
 */
interface BookmarkRepository {
  /**
   * Retrieve a bookmark list, optionally constrained to a single author
   *
   * @param authorPubkey Nostr public key (Bech32-encoded format, starts with npub1) to constrain
   *   the query to a single author (Local tab), or null to query all authors (Global tab)
   * @param until Unix timestamp upper bound for pagination (inclusive per NIP-01), null for latest
   * @return Success(BookmarkList) if found, Success(null) if no bookmarks, Failure on error
   */
  suspend fun getBookmarkList(authorPubkey: String?, until: Long? = null): Result<BookmarkList?>

  /**
   * Create an unsigned bookmark event
   *
   * @param hexPubkey Author's public key (hex-encoded)
   * @param url URL to bookmark (without scheme)
   * @param title Bookmark title
   * @param categories List of categories (t-tags)
   * @param comment Bookmark comment (content)
   * @return Unsigned event ready for NIP-55 signing
   */
  fun createBookmarkEvent(
      hexPubkey: String,
      url: String,
      title: String,
      categories: List<String>,
      comment: String
  ): UnsignedNostrEvent

  /**
   * Publish a signed bookmark event to relays
   *
   * @param signedEventJson Signed event as JSON string
   * @return Result containing PublishResult on success or error on failure
   */
  suspend fun publishBookmark(signedEventJson: String): Result<PublishResult>
}
