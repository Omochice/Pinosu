package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.feature.comments.model.Comment

/**
 * UseCase interface for fetching comments on a bookmark event
 *
 * Fetches kind:1111 comment events that reference a specific bookmark event via e tag.
 */
interface GetCommentsForBookmarkUseCase {
  /**
   * Fetch comments for a bookmark event
   *
   * @param eventId The bookmark event ID to fetch comments for (hex-encoded)
   * @return Success with list of comments (may be empty), or Failure on error
   */
  suspend operator fun invoke(eventId: String): Result<List<Comment>>
}
