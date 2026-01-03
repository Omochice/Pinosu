package io.github.omochice.pinosu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
 * @property getBookmarkListUseCase UseCase for fetching bookmark list
 * @property getLoginStateUseCase UseCase for getting login state
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

  /** Load bookmarks for the logged-in user */
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
                    isLoading = false,
                    bookmarks = bookmarkList?.items ?: emptyList(),
                    rawEventJson = bookmarkList?.rawEventJson,
                    encryptedContent = bookmarkList?.encryptedContent,
                    error = null)
          },
          onFailure = { e ->
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false, error = e.message ?: "Failed to load bookmarks")
          })
    }
  }

  /** Refresh bookmarks */
  fun refresh() {
    loadBookmarks()
  }
}

/**
 * Bookmark screen UI state
 *
 * @property isLoading Whether loading is in progress
 * @property bookmarks List of bookmark items
 * @property rawEventJson Raw event JSON for debugging
 * @property encryptedContent Encrypted content that needs decryption
 * @property error Error message if any
 */
data class BookmarkUiState(
    val isLoading: Boolean = false,
    val bookmarks: List<BookmarkItem> = emptyList(),
    val rawEventJson: String? = null,
    val encryptedContent: String? = null,
    val error: String? = null,
)
