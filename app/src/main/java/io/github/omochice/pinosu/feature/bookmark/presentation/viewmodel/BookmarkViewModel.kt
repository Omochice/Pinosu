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
   * By default this is a lazy load: a tab that already holds items is not refetched, so it is safe
   * to call every time the tab becomes visible. An empty tab is (re)loaded on every visit so a
   * transient failure — including a relay timeout that the pool reports as an empty success —
   * recovers on the next visit instead of sticking on an empty feed. Pass [forceReload] to reload
   * unconditionally for an explicit pull-to-refresh.
   *
   * @param mode The tab to load (Local queries the current user, Global queries all authors)
   * @param forceReload When true, reload even if the tab already holds items
   */
  fun loadTab(mode: BookmarkFilterMode, forceReload: Boolean = false) {
    val tab = _uiState.value.tab(mode)
    if (tab.isLoading) return
    if (!forceReload && tab.items.isNotEmpty()) return
    viewModelScope.launch { performLoad(mode) }
  }

  private suspend fun performLoad(mode: BookmarkFilterMode) {
    _uiState.updateTab(mode) { it.copy(isLoading = true, error = null) }

    val author =
        resolveAuthorQuery(mode, getLoginStateUseCase)
            ?: run {
              _uiState.updateTab(mode) {
                it.copy(
                    isLoading = false,
                    isLoaded = true,
                    items = emptyList(),
                    error = "Not logged in")
              }
              return
            }

    getBookmarkListUseCase(author.npub, until = null)
        .fold(
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
   * fetch the next page. Appends new items to the tab and deduplicates by eventId. The
   * relay-reported [io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkList.hasMore]
   * hint drives whether more pages are requested.
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

    val author =
        resolveAuthorQuery(mode, getLoginStateUseCase)
            ?: run {
              _uiState.updateTab(mode) { it.copy(isLoadingMore = false, hasMoreItems = false) }
              return
            }

    getBookmarkListUseCase(author.npub, until = oldestCreatedAt)
        .fold(
            onSuccess = { bookmarkList ->
              val newItems = bookmarkList?.items ?: emptyList()
              _uiState.updateTab(mode) { current ->
                val existingIds = current.items.mapNotNull { it.eventId }.toSet()
                val uniqueNewItems = newItems.filter { it.eventId !in existingIds }
                // Without this, a timestamp saturated with a full page of events would make the
                // inclusive `until = oldest` cursor refetch that same all-duplicate page forever.
                current.copy(
                    isLoadingMore = false,
                    items = current.items + uniqueNewItems,
                    hasMoreItems = uniqueNewItems.isNotEmpty() && bookmarkList?.hasMore == true)
              }
            },
            onFailure = { _uiState.updateTab(mode) { it.copy(isLoadingMore = false) } })
  }
}

/** The npub used to constrain a tab's relay query; null means all authors (the Global tab). */
private data class AuthorQuery(val npub: String?)

/**
 * Resolves the [AuthorQuery] for [mode]: Global spans all authors, Local is constrained to the
 * logged-in user. Returns null only when a Local load is attempted while logged out, which the
 * caller surfaces as a "Not logged in" state.
 */
private suspend fun resolveAuthorQuery(
    mode: BookmarkFilterMode,
    getLoginState: GetLoginStateUseCase,
): AuthorQuery? =
    when (mode) {
      BookmarkFilterMode.Global -> AuthorQuery(npub = null)
      BookmarkFilterMode.Local -> getLoginState()?.let { AuthorQuery(npub = it.pubkey.npub) }
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
