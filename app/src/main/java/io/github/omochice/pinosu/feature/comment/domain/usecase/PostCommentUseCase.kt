package io.github.omochice.pinosu.feature.comment.domain.usecase

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult

/**
 * Use case interface for posting NIP-22 kind 1111 comments
 *
 * Creates unsigned comment events and publishes signed events to relays.
 */
interface PostCommentUseCase {

  /**
   * Create an unsigned kind 1111 comment event
   *
   * @param content Comment text
   * @param rootPubkey Hex-encoded public key of the bookmark author
   * @param dTag The d-tag of the bookmark event
   * @param rootEventId The event ID of the bookmark event
   * @return Result containing unsigned event on success or error on failure
   */
  suspend fun createUnsignedEvent(
      content: String,
      rootPubkey: String,
      dTag: String,
      rootEventId: String,
  ): Result<UnsignedNostrEvent>

  /**
   * Publish a signed comment event to relays
   *
   * @param signedEventJson Signed event as JSON string
   * @return Result containing PublishResult on success or error on failure
   */
  suspend fun publishSignedEvent(signedEventJson: String): Result<PublishResult>
}
