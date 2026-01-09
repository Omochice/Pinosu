package io.github.omochice.pinosu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
   * Fetches bookmark list from relays and updates UI state accordingly.
   */
  fun loadBookmarks() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)

      val user = getLoginStateUseCase()
      if (user == null) {
        _uiState.value =
            _uiState.value.copy(isLoading = false, error = "Not logged in", bookmarks = emptyList())
        return@launch
      }

      val result = getBookmarkListUseCase(user.pubkey)
      result.fold(
          onSuccess = { bookmarkList ->
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false, bookmarks = bookmarkList?.items ?: emptyList(), error = null)
          },
          onFailure = { e ->
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false, error = e.message ?: "Failed to load bookmarks")
          })
    }
  }

  /** Refresh bookmark list by reloading from relays */
  fun refresh() {
    loadBookmarks()
  }
}
