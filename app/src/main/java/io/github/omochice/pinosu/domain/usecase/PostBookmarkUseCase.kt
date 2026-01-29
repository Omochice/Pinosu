package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult

/**
 * Use case interface for posting bookmarks
 *
 * Creates unsigned bookmark events and publishes signed events to relays.
 */
interface PostBookmarkUseCase {

  /**
   * Create an unsigned bookmark event
   *
   * @param url URL to bookmark (without scheme)
   * @param title Bookmark title
   * @param categories List of categories
   * @param comment Bookmark comment
   * @return Result containing unsigned event on success or error on failure
   */
  suspend fun createUnsignedEvent(
      url: String,
      title: String,
      categories: List<String>,
      comment: String
  ): Result<UnsignedNostrEvent>

  /**
   * Publish a signed bookmark event to relays
   *
   * @param signedEventJson Signed event as JSON string
   * @return Result containing PublishResult on success or error on failure
   */
  suspend fun publishSignedEvent(signedEventJson: String): Result<PublishResult>
}
