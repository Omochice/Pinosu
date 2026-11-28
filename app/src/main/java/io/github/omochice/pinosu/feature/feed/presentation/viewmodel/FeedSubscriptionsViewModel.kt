package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionChange
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedSubscriptionsUseCase
import io.github.omochice.pinosu.feature.feed.domain.usecase.PrepareFeedSubscriptionChangeUseCase
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the feed subscription screen
 *
 * Loads the subscription list when created, and turns each add, remove, or category change into a
 * signer request whose answer is then published.
 *
 * A change requested while another is in progress is ignored.
 */
@HiltViewModel
class FeedSubscriptionsViewModel
@Inject
constructor(
    private val loadSubscriptions: LoadFeedSubscriptionsUseCase,
    private val prepareChange: PrepareFeedSubscriptionChangeUseCase,
    private val publisher: FeedSubscriptionPublisher,
) : ViewModel() {

  private val _uiState = MutableStateFlow(FeedSubscriptionsUiState())
  val uiState: StateFlow<FeedSubscriptionsUiState> = _uiState.asStateFlow()

  /** Signer requests the screen has to launch, each delivered once to a single collector */
  val signRequests: Flow<Intent> = publisher.signRequests

  private var loadJob: Job? = null

  init {
    reload()
  }

  /** Load the subscription list again, keeping a loaded list visible until it loads */
  fun reload() {
    _uiState.update {
      if (it.content is FeedSubscriptionsUiState.Content.Error) {
        it.copy(content = FeedSubscriptionsUiState.Content.Loading)
      } else {
        it
      }
    }
    loadJob?.cancel()
    loadJob =
        viewModelScope.launch {
          val content =
              loadSubscriptions()
                  .fold(
                      onSuccess = { FeedSubscriptionsUiState.Content.Loaded(it) },
                      onFailure = { FeedSubscriptionsUiState.Content.Error(feedLoadErrorOf(it)) })
          _uiState.update { it.copy(content = content) }
        }
  }

  /**
   * Add the feed the user entered or picked
   *
   * @param input A feed URL or a web site URL, with or without a scheme
   */
  fun add(input: String) {
    prepare { prepareChange.add(input) }
  }

  /**
   * Remove a subscribed feed
   *
   * @param feedUrl URL of the feed, as subscribed
   */
  fun remove(feedUrl: String) {
    prepare { prepareChange.remove(feedUrl) }
  }

  /**
   * File a subscribed feed under a category, or take it out of its category
   *
   * @param feedUrl URL of the feed, as subscribed
   * @param category Name of the category, or null or blank for none
   */
  fun setCategory(feedUrl: String, category: String?) {
    prepare { prepareChange.setCategory(feedUrl, category) }
  }

  /** Continue publishing the first list after the user agreed that it is public */
  fun confirmPublicNotice() {
    val dialog = _uiState.value.dialog as? FeedSubscriptionsDialog.ConfirmPublicList ?: return
    _uiState.update { it.copy(isWorking = true, dialog = null) }
    publisher.requestSignature(dialog.event)
  }

  /** Close the open dialog, abandoning the change it was asking about */
  fun dismissDialog() {
    _uiState.update { it.copy(dialog = null) }
  }

  /**
   * Publish the list the signer answered for
   *
   * @param resultCode Activity result code from the signer
   * @param data Intent data from the signer
   */
  fun processSignedEvent(resultCode: Int, data: Intent?) {
    _uiState.update { it.copy(isWorking = true) }
    viewModelScope.launch {
      publisher
          .publishSigned(resultCode, data)
          .onSuccess { result ->
            _uiState.update {
              it.copy(
                  isWorking = false,
                  hasPublished = true,
                  message =
                      FeedSubscriptionsMessage.PartiallyPublished.takeIf {
                        result.failedRelays.isNotEmpty()
                      })
            }
            reload()
          }
          .onFailure { error ->
            _uiState.update { it.copy(isWorking = false, message = publishFailureMessage(error)) }
          }
    }
  }

  /** Forget the message once it has been shown */
  fun dismissMessage() {
    _uiState.update { it.copy(message = null) }
  }

  private fun prepare(change: suspend () -> Result<FeedSubscriptionChange>) {
    if (_uiState.value.isWorking) return
    _uiState.update { it.copy(isWorking = true, dialog = null, message = null) }
    viewModelScope.launch {
      change().onSuccess(::continueWith).onFailure { error ->
        _uiState.update { it.copy(isWorking = false, message = prepareFailureMessage(error)) }
      }
    }
  }

  private fun continueWith(change: FeedSubscriptionChange) {
    if (change is FeedSubscriptionChange.ReadyToSign && !change.isFirstList) {
      publisher.requestSignature(change.event)
      return
    }
    _uiState.update { state ->
      when (change) {
        is FeedSubscriptionChange.ReadyToSign ->
            state.copy(dialog = FeedSubscriptionsDialog.ConfirmPublicList(change.event))
        is FeedSubscriptionChange.ChooseCandidate ->
            state.copy(dialog = FeedSubscriptionsDialog.ChooseCandidate(change.candidates))
        is FeedSubscriptionChange.AlreadySubscribed ->
            state.copy(message = FeedSubscriptionsMessage.AlreadySubscribed)
        is FeedSubscriptionChange.NotSubscribed,
        is FeedSubscriptionChange.CategoryUnchanged -> state
      }.copy(isWorking = false)
    }
    if (change is FeedSubscriptionChange.NotSubscribed) reload()
  }
}
