package io.github.omochice.pinosu.feature.comment.domain.usecase

import io.github.omochice.pinosu.feature.comment.domain.model.Comment

/**
 * Use case interface for fetching comments on a bookmark
 *
 * Combines the bookmark's own content (as an author comment) with kind 1111 relay comments.
 */
interface GetCommentsForBookmarkUseCase {

  /**
   * Fetch comments for a bookmark event
   *
   * If [authorContent] is non-empty, a synthetic author comment is prepended to the result. Relay
   * comments are sorted by createdAt ascending.
   *
   * @param rootPubkey Hex-encoded public key of the bookmark author
   * @param dTag The d-tag of the bookmark event
   * @param rootEventId The event ID of the bookmark event
   * @param authorContent The bookmark event's content field (may be empty)
   * @param authorCreatedAt The bookmark event's created_at timestamp
   * @return Result containing list of Comments on success or error on failure
   */
  suspend operator fun invoke(
      rootPubkey: String,
      dTag: String,
      rootEventId: String,
      authorContent: String,
      authorCreatedAt: Long,
  ): Result<List<Comment>>
}
