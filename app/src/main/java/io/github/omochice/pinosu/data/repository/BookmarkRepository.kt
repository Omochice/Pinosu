package io.github.omochice.pinosu.data.repository

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
   * Constructs a kind:39701 event with the provided bookmark data. The URL scheme (http:// or
   * https://) is automatically removed for the 'd' tag identifier. The event is unsigned and needs
   * to be signed by a NIP-55 signer before publishing.
   *
   * @param pubkey Author's public key (hex-encoded)
   * @param url Bookmark URL (scheme will be removed for 'd' tag, preserved with https:// default
   *   for 'r' tag)
   * @param title Optional bookmark title
   * @param categories List of category tags
   * @param comment Optional comment/note about the bookmark
   * @return Success(UnsignedNostrEvent) or Failure on error
   */
  suspend fun createBookmarkEvent(
      pubkey: String,
      url: String,
      title: String? = null,
      categories: List<String> = emptyList(),
      comment: String = ""
  ): Result<UnsignedNostrEvent>

  /**
   * Publish a signed bookmark event to relays
   *
   * @param signedEventJson Complete signed event JSON string (includes id, sig, etc.)
   * @return Success if published to at least one relay, Failure otherwise
   */
  suspend fun publishBookmark(signedEventJson: String): Result<Unit>
}
