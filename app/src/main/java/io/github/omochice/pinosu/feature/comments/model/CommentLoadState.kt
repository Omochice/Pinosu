package io.github.omochice.pinosu.feature.comments.model

/** Loading state for comments on a bookmark */
sealed class CommentLoadState {
  /** Comments are being fetched */
  data object Loading : CommentLoadState()

  /** Comments loaded successfully */
  data class Success(val comments: List<Comment>) : CommentLoadState()

  /** Failed to load comments */
  data class Error(val message: String) : CommentLoadState()
}
