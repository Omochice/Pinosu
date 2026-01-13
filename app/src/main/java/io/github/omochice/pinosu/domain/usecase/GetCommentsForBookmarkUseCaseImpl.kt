package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.CommentRepository
import io.github.omochice.pinosu.feature.comments.model.Comment
import javax.inject.Inject

/**
 * Implementation of GetCommentsForBookmarkUseCase
 *
 * Delegates to CommentRepository to fetch comment events from relays.
 *
 * @property commentRepository Repository for comment data access
 */
class GetCommentsForBookmarkUseCaseImpl
@Inject
constructor(private val commentRepository: CommentRepository) : GetCommentsForBookmarkUseCase {

  /**
   * Fetch comments for a bookmark event
   *
   * @param eventId The bookmark event ID to fetch comments for (hex-encoded)
   * @return Success with list of comments (may be empty), or Failure on error
   */
  override suspend fun invoke(eventId: String): Result<List<Comment>> {
    return commentRepository.getCommentsForEvent(eventId)
  }
}
