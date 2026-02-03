package io.github.omochice.pinosu.feature.comment.domain.usecase

import io.github.omochice.pinosu.core.nip.nip19.Nip19EventResolver
import io.github.omochice.pinosu.feature.comment.data.repository.CommentRepository
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GetCommentsForBookmarkUseCase
 *
 * Uses [CommentRepository] for fetching kind 1111 comments from relays. When `authorContent`
 * contains `nostr:nevent1...` references, resolves them by fetching the referenced events and
 * displaying their content as author comments.
 */
@Singleton
class GetCommentsForBookmarkUseCaseImpl
@Inject
constructor(
    private val commentRepository: CommentRepository,
    private val nip19EventResolver: Nip19EventResolver,
) : GetCommentsForBookmarkUseCase {

  override suspend fun invoke(
      rootPubkey: String,
      dTag: String,
      rootEventId: String,
      authorContent: String,
      authorCreatedAt: Long,
  ): Result<List<Comment>> {
    val relayResult = commentRepository.getCommentsForBookmark(rootPubkey, dTag, rootEventId)
    val relayComments =
        relayResult.getOrElse {
          return Result.failure(it)
        }
    val sorted = relayComments.sortedBy { it.createdAt }

    return if (authorContent.isNotBlank()) {
      val authorComments =
          resolveAuthorContent(authorContent, rootPubkey, rootEventId, authorCreatedAt)
      Result.success(authorComments + sorted)
    } else {
      Result.success(sorted)
    }
  }

  private suspend fun resolveAuthorContent(
      authorContent: String,
      rootPubkey: String,
      rootEventId: String,
      authorCreatedAt: Long,
  ): List<Comment> {
    val eventIds = nip19EventResolver.extractEventIds(authorContent)

    if (eventIds.isEmpty()) {
      return listOf(
          Comment(
              id = "author-content-$rootEventId",
              content = authorContent,
              authorPubkey = rootPubkey,
              createdAt = authorCreatedAt,
              isAuthorComment = true))
    }

    val fetchResult = commentRepository.getEventsByIds(eventIds)
    return fetchResult
        .map { events ->
          events.map { event ->
            Comment(
                id = event.id,
                content = event.content,
                authorPubkey = event.pubkey,
                createdAt = event.createdAt,
                isAuthorComment = true,
                kind = event.kind)
          }
        }
        .getOrElse {
          listOf(
              Comment(
                  id = "author-content-$rootEventId",
                  content = authorContent,
                  authorPubkey = rootPubkey,
                  createdAt = authorCreatedAt,
                  isAuthorComment = true))
        }
  }
}
