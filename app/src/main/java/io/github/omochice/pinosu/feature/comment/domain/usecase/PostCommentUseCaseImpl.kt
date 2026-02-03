package io.github.omochice.pinosu.feature.comment.domain.usecase

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.comment.data.repository.CommentRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PostCommentUseCase
 *
 * Uses [CommentRepository] for comment operations and [GetLoginStateUseCase] for getting the
 * current user.
 */
@Singleton
class PostCommentUseCaseImpl
@Inject
constructor(
    private val commentRepository: CommentRepository,
    private val getLoginStateUseCase: GetLoginStateUseCase,
) : PostCommentUseCase {

  override suspend fun createUnsignedEvent(
      content: String,
      rootPubkey: String,
      dTag: String,
      rootEventId: String,
  ): Result<UnsignedNostrEvent> {
    val user =
        getLoginStateUseCase() ?: return Result.failure(IllegalStateException("User not logged in"))

    val hexPubkey =
        user.pubkey.hex ?: return Result.failure(IllegalArgumentException("Invalid npub format"))

    return Result.success(
        commentRepository.createCommentEvent(
            hexPubkey = hexPubkey,
            content = content,
            rootPubkey = rootPubkey,
            dTag = dTag,
            rootEventId = rootEventId))
  }

  override suspend fun publishSignedEvent(signedEventJson: String): Result<PublishResult> =
      commentRepository.publishComment(signedEventJson)
}
