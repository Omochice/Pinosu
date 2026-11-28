package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.feature.feed.domain.model.FeedTimeline
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedTimelineUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the feed timeline screen
 *
 * @param loadFeedTimeline UseCase that builds the timeline from the user's subscriptions
 * @param loadedItems Holder the shown items are handed to, so the reader can open them
 * @param observeDisplayMode UseCase observing whether to lay the timeline out as a list or a grid
 */
@HiltViewModel
class FeedViewModel
@Inject
constructor(
    private val loadFeedTimeline: LoadFeedTimelineUseCase,
    private val loadedItems: LoadedFeedItems,
    observeDisplayMode: ObserveDisplayModeUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(FeedUiState())
  val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

  private var loadJob: Job? = null

  init {
    observeDisplayMode()
        .onEach { mode -> _uiState.update { it.copy(displayMode = mode) } }
        .launchIn(viewModelScope)
    load()
  }

  /** Reload the timeline, keeping the current content visible until the reload finishes */
  fun refresh() {
    if (loadJob?.isActive == true) return
    _uiState.update { it.copy(isRefreshing = true) }
    load()
  }

  /**
   * Reload the timeline because the subscription list changed
   *
   * Unlike [refresh], a load already running is restarted, because it may have read the list from
   * before the change.
   */
  fun reloadAfterSubscriptionChange() {
    loadJob?.cancel()
    _uiState.update { it.copy(isRefreshing = it.content != FeedUiState.Content.Loading) }
    load()
  }

  /**
   * Select the tab of a category
   *
   * @param category Category whose tab the user selected, or null for the tab showing every feed
   */
  fun selectCategory(category: String?) {
    _uiState.update { it.copy(selectedCategory = category) }
  }

  private fun load() {
    loadJob =
        viewModelScope.launch {
          val timeline = loadFeedTimeline()
          val content = timeline.fold(onSuccess = ::toContent, onFailure = ::toError)
          val categories = orderedCategories(timeline.getOrNull()?.categories.orEmpty())
          loadedItems.replace((content as? FeedUiState.Content.Loaded)?.items.orEmpty())
          _uiState.update { current ->
            val selected = current.selectedCategory?.takeIf { it in categories }
            current.copy(
                content = content,
                isRefreshing = false,
                categories = categories,
                selectedCategory = selected)
          }
        }
  }

  private fun toContent(timeline: FeedTimeline): FeedUiState.Content =
      if (timeline.items.isEmpty() && timeline.failedFeeds.isEmpty()) {
        FeedUiState.Content.Empty
      } else {
        FeedUiState.Content.Loaded(
            timeline.items.distinctBy { it.feedUrl to it.id }, timeline.failedFeeds)
      }

  private fun toError(error: Throwable): FeedUiState.Content =
      FeedUiState.Content.Error(feedLoadErrorOf(error))
}
