package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.comments.model.Comment

/**
 * UI state for bookmark list screen
 *
 * @property isLoading Whether bookmark data is currently being loaded
 * @property allBookmarks Complete list of all bookmark items from relay (shared data pool)
 * @property bookmarks Filtered list of bookmark items to display based on selected tab
 * @property error Error message if loading failed
 * @property selectedBookmarkForUrlDialog Bookmark item for which URL selection dialog is shown
 * @property urlOpenError Error message when URL opening fails
 * @property selectedTab Currently selected filter tab (Local or Global)
 * @property userHexPubkey Hex-encoded pubkey of logged-in user for local filtering
 * @property expandedCommentEventIds Set of event IDs for which comments section is expanded
 * @property commentsMap Map of event ID to comment loading state
 */
data class BookmarkUiState(
    val isLoading: Boolean = false,
    val allBookmarks: List<BookmarkItem> = emptyList(),
    val bookmarks: List<BookmarkItem> = emptyList(),
    val error: String? = null,
    val selectedBookmarkForUrlDialog: BookmarkItem? = null,
    val urlOpenError: String? = null,
    val selectedTab: BookmarkFilterMode = BookmarkFilterMode.Local,
    val userHexPubkey: String? = null,
    val expandedCommentEventIds: Set<String> = emptySet(),
    val commentsMap: Map<String, CommentLoadState> = emptyMap(),
)

/** Loading state for comments on a bookmark */
sealed class CommentLoadState {
  /** Comments are being fetched */
  data object Loading : CommentLoadState()

  /** Comments loaded successfully */
  data class Success(val comments: List<Comment>) : CommentLoadState()

  /** Failed to load comments */
  data class Error(val message: String) : CommentLoadState()
}
