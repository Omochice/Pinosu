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
   * Load the first page for [mode]
   *
   * By default this is a lazy, idempotent load: it does nothing when the tab is already loading or
   * has completed a load, so it is safe to call every time the tab becomes visible. Pass
   * [forceReload] to bypass that guard for an explicit pull-to-refresh.
   *
   * @param mode The tab to load (Local queries the current user, Global queries all authors)
   * @param forceReload When true, reload even if the tab has already been loaded
   */
  fun loadTab(mode: BookmarkFilterMode, forceReload: Boolean = false) {
    val tab = _uiState.value.tab(mode)
    if (!forceReload && (tab.isLoading || tab.isLoaded)) return
    viewModelScope.launch { performLoad(mode) }
  }

  private suspend fun performLoad(mode: BookmarkFilterMode) {
    _uiState.updateTab(mode) { it.copy(isLoading = true, error = null) }

    val authorPubkey =
        when (mode) {
          BookmarkFilterMode.Global -> null
          BookmarkFilterMode.Local ->
              (getLoginStateUseCase()
                      ?: run {
                        _uiState.updateTab(mode) {
                          it.copy(
                              isLoading = false,
                              isLoaded = true,
                              items = emptyList(),
                              error = "Not logged in")
                        }
                        return
                      })
                  .pubkey
                  .npub
        }

    val result = getBookmarkListUseCase(authorPubkey, until = null)
    result.fold(
        onSuccess = { bookmarkList ->
          _uiState.updateTab(mode) {
            it.copy(
                isLoading = false,
                isLoadingMore = false,
                isLoaded = true,
                items = bookmarkList?.items ?: emptyList(),
                hasMoreItems = bookmarkList?.hasMore ?: false,
                error = null)
          }
        },
        onFailure = { e ->
          _uiState.updateTab(mode) {
            it.copy(
                isLoading = false,
                isLoadingMore = false,
                isLoaded = true,
                error = e.message ?: "Failed to load bookmarks")
          }
        })
  }

  /**
   * Select bookmark filter tab
   *
   * Updates the selected tab state. Filtering is handled by the Composable layer (BookmarkPager).
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
   * Load older bookmarks for infinite scroll pagination on [mode]
   *
   * Uses the oldest fetched bookmark's createdAt as the `until` cursor (inclusive per NIP-01) to
   * fetch the next page. Appends new items to the tab and deduplicates by eventId. Stops paginating
   * when a page yields no new items so that duplicate boundary events cannot cause a runaway load.
   *
   * @param mode The tab to paginate
   */
  fun loadMore(mode: BookmarkFilterMode) {
    val tab = _uiState.value.tab(mode)
    if (tab.isLoadingMore || !tab.hasMoreItems) return

    val oldestCreatedAt = tab.items.mapNotNull { it.event?.createdAt }.minOrNull() ?: return

    viewModelScope.launch { performLoadMore(mode, oldestCreatedAt) }
  }

  private suspend fun performLoadMore(mode: BookmarkFilterMode, oldestCreatedAt: Long) {
    _uiState.updateTab(mode) { it.copy(isLoadingMore = true) }

    val authorPubkey =
        when (mode) {
          BookmarkFilterMode.Global -> null
          BookmarkFilterMode.Local ->
              (getLoginStateUseCase()
                      ?: run {
                        _uiState.updateTab(mode) { it.copy(isLoadingMore = false) }
                        return
                      })
                  .pubkey
                  .npub
        }

    val result = getBookmarkListUseCase(authorPubkey, until = oldestCreatedAt)
    result.fold(
        onSuccess = { bookmarkList ->
          val newItems = bookmarkList?.items ?: emptyList()
          _uiState.updateTab(mode) { current ->
            val existingIds = current.items.mapNotNull { it.eventId }.toSet()
            val uniqueNewItems = newItems.filter { it.eventId !in existingIds }
            current.copy(
                isLoadingMore = false,
                items = current.items + uniqueNewItems,
                hasMoreItems = uniqueNewItems.isNotEmpty() && bookmarkList?.hasMore ?: false)
          }
        },
        onFailure = { _uiState.updateTab(mode) { it.copy(isLoadingMore = false) } })
  }
}

/**
 * Applies [transform] to the [BookmarkTabState] identified by [mode], leaving the other tab
 * untouched. Kept as a top-level helper so the ViewModel's state-mutation logic lives in one place
 * without inflating the class surface.
 */
private fun MutableStateFlow<BookmarkUiState>.updateTab(
    mode: BookmarkFilterMode,
    transform: (BookmarkTabState) -> BookmarkTabState,
) {
  update { state ->
    when (mode) {
      BookmarkFilterMode.Local -> state.copy(local = transform(state.local))
      BookmarkFilterMode.Global -> state.copy(global = transform(state.global))
    }
  }
}
