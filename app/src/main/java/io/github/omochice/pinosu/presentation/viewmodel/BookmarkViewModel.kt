package io.github.omochice.pinosu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.data.util.Bech32
import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.comments.usecase.GetCommentsForBookmarkUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for bookmark list screen
 *
 * Manages bookmark data loading and UI state for the bookmark list display.
 *
 * @property getBookmarkListUseCase UseCase for fetching bookmark list
 * @property getLoginStateUseCase UseCase for retrieving current login state
 * @property getCommentsForBookmarkUseCase UseCase for fetching comments on bookmark events
 */
@HiltViewModel
class BookmarkViewModel
@Inject
constructor(
    private val getBookmarkListUseCase: GetBookmarkListUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val getCommentsForBookmarkUseCase: GetCommentsForBookmarkUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(BookmarkUiState())
  val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

  /**
   * Load bookmarks for the current logged-in user
   *
   * Fetches all bookmarks from relays, stores in shared data pool, and applies filter based on
   * selected tab.
   */
  fun loadBookmarks() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)

      val user = getLoginStateUseCase()
      if (user == null) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                error = "Not logged in",
                allBookmarks = emptyList(),
                bookmarks = emptyList())
        return@launch
      }

      val userHexPubkey = Bech32.npubToHex(user.pubkey)

      val result = getBookmarkListUseCase(user.pubkey)
      result.fold(
          onSuccess = { bookmarkList ->
            val allItems = bookmarkList?.items ?: emptyList()
            _uiState.update { state ->
              val updatedState =
                  state.copy(
                      isLoading = false,
                      allBookmarks = allItems,
                      userHexPubkey = userHexPubkey,
                      error = null)
              updatedState.copy(bookmarks = filterBookmarks(updatedState))
            }
          },
          onFailure = { e ->
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false, error = e.message ?: "Failed to load bookmarks")
          })
    }
  }

  /**
   * Select bookmark filter tab
   *
   * Applies filter based on selected tab without re-fetching data from relay.
   *
   * @param tab The filter mode to select (Local or Global)
   */
  fun selectTab(tab: BookmarkFilterMode) {
    _uiState.update { state ->
      if (state.selectedTab != tab) {
        val newState = state.copy(selectedTab = tab)
        newState.copy(bookmarks = filterBookmarks(newState))
      } else {
        state
      }
    }
  }

  /**
   * Filter bookmarks based on selected tab
   *
   * Local tab shows only bookmarks authored by the logged-in user. Global tab shows all bookmarks.
   *
   * @param state Current UI state to filter from
   * @return Filtered list of bookmarks
   */
  private fun filterBookmarks(state: BookmarkUiState): List<BookmarkItem> {
    return when (state.selectedTab) {
      BookmarkFilterMode.Local -> {
        state.userHexPubkey?.let { hexPubkey ->
          state.allBookmarks.filter { it.event?.author == hexPubkey }
        } ?: emptyList()
      }
      BookmarkFilterMode.Global -> state.allBookmarks
    }
  }

  /** Refresh bookmark list by reloading from relays */
  fun refresh() {
    loadBookmarks()
  }

  /**
   * Handle bookmark card click
   *
   * Shows URL selection dialog for bookmarks with multiple URLs
   */
  fun onBookmarkCardClicked(bookmark: BookmarkItem) {
    _uiState.value = _uiState.value.copy(selectedBookmarkForUrlDialog = bookmark)
  }

  /** Dismiss URL selection dialog */
  fun dismissUrlDialog() {
    _uiState.value = _uiState.value.copy(selectedBookmarkForUrlDialog = null)
  }

  /**
   * Set URL opening error message
   *
   * @param error Error message to display
   */
  fun setUrlOpenError(error: String) {
    _uiState.value = _uiState.value.copy(urlOpenError = error)
  }

  /** Dismiss error dialog */
  fun dismissErrorDialog() {
    _uiState.value = _uiState.value.copy(urlOpenError = null)
  }

  /**
   * Toggle comment section expansion for a bookmark
   *
   * Expands or collapses the comment section. When expanding, triggers fetch if comments not
   * loaded.
   *
   * @param eventId The event ID of the bookmark to toggle comments for
   */
  fun toggleComments(eventId: String) {
    val currentState = _uiState.value
    val isExpanded = currentState.expandedCommentEventIds.contains(eventId)

    if (isExpanded) {
      _uiState.update { state ->
        state.copy(expandedCommentEventIds = state.expandedCommentEventIds - eventId)
      }
    } else {
      _uiState.update { state ->
        state.copy(expandedCommentEventIds = state.expandedCommentEventIds + eventId)
      }
      if (!currentState.commentsMap.containsKey(eventId)) {
        fetchCommentsForBookmark(eventId)
      }
    }
  }

  /**
   * Fetch comments for a bookmark event
   *
   * @param eventId The event ID to fetch comments for
   */
  private fun fetchCommentsForBookmark(eventId: String) {
    viewModelScope.launch {
      _uiState.update { state ->
        state.copy(commentsMap = state.commentsMap + (eventId to CommentLoadState.Loading))
      }

      val result = getCommentsForBookmarkUseCase(eventId)
      result.fold(
          onSuccess = { comments ->
            _uiState.update { state ->
              state.copy(
                  commentsMap = state.commentsMap + (eventId to CommentLoadState.Success(comments)))
            }
          },
          onFailure = { e ->
            _uiState.update { state ->
              state.copy(
                  commentsMap =
                      state.commentsMap +
                          (eventId to
                              CommentLoadState.Error(e.message ?: "Failed to load comments")))
            }
          })
    }
  }
}
