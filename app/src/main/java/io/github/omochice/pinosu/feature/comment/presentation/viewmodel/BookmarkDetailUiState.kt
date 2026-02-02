package io.github.omochice.pinosu.feature.comment.presentation.viewmodel

import io.github.omochice.pinosu.feature.comment.domain.model.Comment

/**
 * UI state for bookmark detail screen
 *
 * @property isLoading Whether comments are being fetched
 * @property comments List of comments (author comment + kind 1111 relay comments)
 * @property commentInput Current text in the comment input field
 * @property isSubmitting Whether a comment is being posted
 * @property error Error message to display
 * @property postSuccess Whether the comment was posted successfully
 */
data class BookmarkDetailUiState(
    val isLoading: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val commentInput: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val postSuccess: Boolean = false,
)
