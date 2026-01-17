package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.model.UnsignedNostrEvent

/**
 * UseCase interface for posting bookmarks to Nostr relays
 *
 * Provides methods to create unsigned bookmark events and publish signed events.
 */
interface PostBookmarkUseCase {
  /**
   * Create an unsigned bookmark event
   *
   * @param pubkey Author's public key (hex-encoded)
   * @param url Bookmark URL
   * @param title Optional bookmark title
   * @param categories List of category tags
   * @param comment Optional comment/note
   * @return Success(UnsignedNostrEvent) or Failure on error
   */
  suspend fun createUnsignedEvent(
      pubkey: String,
      url: String,
      title: String?,
      categories: List<String>,
      comment: String
  ): Result<UnsignedNostrEvent>

  /**
   * Publish a signed bookmark event to relays
   *
   * @param signedEventJson Complete signed event JSON string
   * @return Success if published successfully, Failure otherwise
   */
  suspend fun publishSignedEvent(signedEventJson: String): Result<Unit>
}
