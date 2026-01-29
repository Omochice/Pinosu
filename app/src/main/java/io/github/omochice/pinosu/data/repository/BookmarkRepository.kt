package io.github.omochice.pinosu.data.repository

import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.data.model.UnsignedNostrEvent
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
