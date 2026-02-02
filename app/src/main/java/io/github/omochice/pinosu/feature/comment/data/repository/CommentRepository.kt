package io.github.omochice.pinosu.feature.comment.data.repository

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.comment.domain.model.Comment

/**
 * Repository interface for NIP-22 comment data access
 *
 * Provides access to kind 1111 comment events from relays.
 */
interface CommentRepository {

  /**
   * Fetch kind 1111 comments for a kind 39701 bookmark event
   *
   * @param rootPubkey Hex-encoded public key of the bookmark author
   * @param dTag The d-tag of the bookmark event (URL without scheme)
   * @param rootEventId The event ID of the bookmark event
   * @return Result containing list of Comments on success or error on failure
   */
  suspend fun getCommentsForBookmark(
      rootPubkey: String,
      dTag: String,
      rootEventId: String
  ): Result<List<Comment>>

  /**
   * Create an unsigned kind 1111 comment event with NIP-22 tags
   *
   * @param hexPubkey Author's public key (hex-encoded)
   * @param content Comment text
   * @param rootPubkey Hex-encoded public key of the bookmark author
   * @param dTag The d-tag of the bookmark event
   * @param rootEventId The event ID of the bookmark event
   * @return Unsigned event ready for NIP-55 signing
   */
  fun createCommentEvent(
      hexPubkey: String,
      content: String,
      rootPubkey: String,
      dTag: String,
      rootEventId: String
  ): UnsignedNostrEvent

  /**
   * Publish a signed comment event to relays
   *
   * @param signedEventJson Signed event as JSON string
   * @return Result containing PublishResult on success or error on failure
   */
  suspend fun publishComment(signedEventJson: String): Result<PublishResult>
}
