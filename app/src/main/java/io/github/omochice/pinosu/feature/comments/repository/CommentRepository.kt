package io.github.omochice.pinosu.feature.comments.repository

import io.github.omochice.pinosu.feature.comments.model.Comment

/**
 * Repository interface for fetching Nostr kind:1111 comments
 *
 * Provides access to comments that reference other Nostr events via the `e` tag.
 */
interface CommentRepository {
  /**
   * Fetch comments for a specific event
   *
   * @param eventId The event ID to fetch comments for (hex-encoded)
   * @return Success with list of comments (may be empty), or Failure on error
   */
  suspend fun getCommentsForEvent(eventId: String): Result<List<Comment>>
}
