package io.github.omochice.pinosu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.data.util.Bech32
import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for bookmark list screen
 *
 * Manages bookmark data loading and UI state for the bookmark list display.
 *
 * @property getBookmarkListUseCase UseCase for fetching bookmark list
 * @property getLoginStateUseCase UseCase for retrieving current login state
 */
@HiltViewModel
class BookmarkViewModel
@Inject
constructor(
    private val getBookmarkListUseCase: GetBookmarkListUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
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
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    allBookmarks = allItems,
                    userHexPubkey = userHexPubkey,
                    error = null)
            applyFilter()
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
    if (_uiState.value.selectedTab != tab) {
      _uiState.value = _uiState.value.copy(selectedTab = tab)
      applyFilter()
    }
  }

  /**
   * Apply filter to bookmarks based on selected tab
   *
   * Local tab shows only bookmarks authored by the logged-in user. Global tab shows all bookmarks.
   */
  private fun applyFilter() {
    val state = _uiState.value
    val filtered =
        when (state.selectedTab) {
          BookmarkFilterMode.Local -> {
            val hexPubkey = state.userHexPubkey
            if (hexPubkey != null) {
              state.allBookmarks.filter { it.event?.author == hexPubkey }
            } else {
              emptyList()
            }
          }
          BookmarkFilterMode.Global -> state.allBookmarks
        }
    _uiState.value = state.copy(bookmarks = filtered)
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
}
