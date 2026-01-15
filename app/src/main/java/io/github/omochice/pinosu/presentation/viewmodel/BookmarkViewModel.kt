@file:OptIn(kotlinx.coroutines.FlowPreview::class)

package io.github.omochice.pinosu.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.data.relay.RelayConfig
import io.github.omochice.pinosu.data.util.Bech32
import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
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

  companion object {
    private const val TAG = "BookmarkViewModel"
    private const val DEFAULT_RELAY_URL = "wss://yabu.me"
    private const val RELAY_DEBOUNCE_MS = 300L
  }

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
   * Observe relay flow and load bookmarks incrementally
   *
   * Subscribes to relay flow with 300ms debounce. When relays arrive, fetches bookmarks using those
   * relays and merges with existing results (deduplicating by event ID). Falls back to default
   * relay (yabu.me) only when no relays are received from the flow.
   *
   * @param relayFlow Flow emitting available relay configurations
   */
  fun observeRelaysAndLoadBookmarks(relayFlow: Flow<List<RelayConfig>>) {
    viewModelScope.launch {
      val user = getLoginStateUseCase()
      if (user == null) {
        return@launch
      }

      val userHexPubkey = Bech32.npubToHex(user.pubkey)
      var hasReceivedRelays = false

      relayFlow
          .filter { it.isNotEmpty() }
          .distinctUntilChanged()
          .debounce(RELAY_DEBOUNCE_MS)
          .onCompletion {
            if (!hasReceivedRelays) {
              Log.d(TAG, "No relays received, using default relay")
              loadBookmarksWithRelays(
                  user.pubkey, userHexPubkey, listOf(RelayConfig(url = DEFAULT_RELAY_URL)))
            }
          }
          .collect { relays ->
            hasReceivedRelays = true
            Log.d(TAG, "Loading bookmarks with ${relays.size} relays")
            loadBookmarksWithRelays(user.pubkey, userHexPubkey, relays)
          }
    }
  }

  /**
   * Load bookmarks using specified relays and merge with existing results
   *
   * @param pubkey User's public key
   * @param userHexPubkey User's hex-encoded public key for filtering
   * @param relays List of relays to query
   */
  private suspend fun loadBookmarksWithRelays(
      pubkey: String,
      userHexPubkey: String?,
      relays: List<RelayConfig>
  ) {
    val result = getBookmarkListUseCase(pubkey, relays)
    result.fold(
        onSuccess = { bookmarkList ->
          val newItems = bookmarkList?.items ?: emptyList()
          _uiState.update { state ->
            val merged = (state.allBookmarks + newItems).distinctBy { it.eventId }
            val updatedState =
                state.copy(
                    isLoading = false,
                    allBookmarks = merged,
                    userHexPubkey = userHexPubkey,
                    error = null)
            updatedState.copy(bookmarks = filterBookmarks(updatedState))
          }
        },
        onFailure = { e -> Log.w(TAG, "Failed to load bookmarks: ${e.message}") })
  }
}
