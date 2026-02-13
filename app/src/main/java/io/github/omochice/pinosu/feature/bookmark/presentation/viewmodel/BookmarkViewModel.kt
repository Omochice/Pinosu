package io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.bookmark.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for bookmark list screen
 *
 * Manages bookmark data loading and UI state for the bookmark list display. Observes display mode
 * changes for immediate UI updates.
 *
 * @param getBookmarkListUseCase UseCase for fetching bookmark list from relays
 * @param getLoginStateUseCase UseCase for retrieving current login state
 * @param observeDisplayModeUseCase UseCase for observing display mode preference changes
 */
@HiltViewModel
class BookmarkViewModel
@Inject
constructor(
    private val getBookmarkListUseCase: GetBookmarkListUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val observeDisplayModeUseCase: ObserveDisplayModeUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(BookmarkUiState())
  val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

  init {
    observeDisplayMode()
  }

  /** Observe display mode preference changes for reactive updates */
  private fun observeDisplayMode() {
    observeDisplayModeUseCase()
        .onEach { displayMode -> _uiState.update { it.copy(displayMode = displayMode) } }
        .launchIn(viewModelScope)
  }

  /**
   * Load bookmarks for the current logged-in user
   *
   * Fetches all bookmarks from relays, stores in shared data pool, and applies filter based on
   * selected tab.
   */
  fun loadBookmarks() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)

      val user =
          getLoginStateUseCase()
              ?: run {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        error = "Not logged in",
                        allBookmarks = emptyList(),
                        bookmarks = emptyList())
                return@launch
              }

      val userHexPubkey = user.pubkey.hex

      val result = getBookmarkListUseCase(user.pubkey.npub)
      result.fold(
          onSuccess = { bookmarkList ->
            val allItems = bookmarkList?.items ?: emptyList()
            _uiState.update { state ->
              state.copy(
                  isLoading = false,
                  allBookmarks = allItems,
                  userHexPubkey = userHexPubkey,
                  error = null)
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
        state.copy(selectedTab = tab)
      } else {
        state
      }
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
}
