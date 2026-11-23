package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.FeedTimeline
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedTimelineUseCase
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the feed timeline screen
 *
 * Loads the timeline once when created, which happens when the screen opens.
 *
 * @param loadFeedTimeline UseCase that builds the timeline from the user's subscriptions
 */
@HiltViewModel
class FeedViewModel @Inject constructor(private val loadFeedTimeline: LoadFeedTimelineUseCase) :
    ViewModel() {

  private val _uiState = MutableStateFlow(FeedUiState())
  val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

  private var loadJob: Job? = null

  init {
    load()
  }

  /** Reload the timeline, keeping the current content visible until the reload finishes */
  fun refresh() {
    if (loadJob?.isActive == true) return
    _uiState.update { it.copy(isRefreshing = true) }
    load()
  }

  private fun load() {
    loadJob =
        viewModelScope.launch {
          val content = loadFeedTimeline().fold(onSuccess = ::toContent, onFailure = ::toError)
          _uiState.update { FeedUiState(content = content, isRefreshing = false) }
        }
  }

  private fun toContent(timeline: FeedTimeline): FeedUiState.Content =
      if (timeline.items.isEmpty() && timeline.failedFeeds.isEmpty()) {
        FeedUiState.Content.Empty
      } else {
        FeedUiState.Content.Loaded(
            timeline.items.distinctBy { it.feedUrl to it.id }, timeline.failedFeeds)
      }

  private fun toError(error: Throwable): FeedUiState.Content {
    val reason =
        if (error is FeedSubscriptionListUnavailableException) {
          when (error.reason) {
            FeedSubscriptionLoadResult.Reason.NO_USER_RELAYS -> FeedLoadError.NoUserRelays
            FeedSubscriptionLoadResult.Reason.ACCESS_DENIED -> FeedLoadError.AccessDenied
            FeedSubscriptionLoadResult.Reason.UNREACHABLE -> FeedLoadError.Unreachable
          }
        } else {
          FeedLoadError.Unknown
        }
    return FeedUiState.Content.Error(reason)
  }
}
