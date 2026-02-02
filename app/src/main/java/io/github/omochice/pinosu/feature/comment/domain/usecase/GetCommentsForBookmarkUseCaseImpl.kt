package io.github.omochice.pinosu.feature.comment.domain.usecase

import io.github.omochice.pinosu.feature.comment.data.repository.CommentRepository
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GetCommentsForBookmarkUseCase
 *
 * Uses [CommentRepository] for fetching kind 1111 comments from relays.
 */
@Singleton
class GetCommentsForBookmarkUseCaseImpl
@Inject
constructor(private val commentRepository: CommentRepository) : GetCommentsForBookmarkUseCase {

  override suspend fun invoke(
      rootPubkey: String,
      dTag: String,
      rootEventId: String,
      authorContent: String,
      authorCreatedAt: Long,
  ): Result<List<Comment>> {
    return commentRepository.getCommentsForBookmark(rootPubkey, dTag, rootEventId).map {
        relayComments ->
      val sorted = relayComments.sortedBy { it.createdAt }

      if (authorContent.isNotEmpty()) {
        val authorComment =
            Comment(
                id = "author-content-$rootEventId",
                content = authorContent,
                authorPubkey = rootPubkey,
                createdAt = authorCreatedAt,
                isAuthorComment = true)
        listOf(authorComment) + sorted
      } else {
        sorted
      }
    }
  }
}
