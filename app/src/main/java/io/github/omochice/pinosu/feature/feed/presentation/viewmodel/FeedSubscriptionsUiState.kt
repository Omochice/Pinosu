package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

/**
 * UI state of the feed subscription screen
 *
 * @property content What the list of subscribed feeds currently shows
 * @property isWorking Whether a change is being prepared, signed, or published
 * @property dialog Question the user has to answer before the change continues, or null
 * @property message Outcome to tell the user once, or null
 * @property hasPublished Whether a changed list has been published since the screen opened
 */
data class FeedSubscriptionsUiState(
    val content: Content = Content.Loading,
    val isWorking: Boolean = false,
    val dialog: FeedSubscriptionsDialog? = null,
    val message: FeedSubscriptionsMessage? = null,
    val hasPublished: Boolean = false,
) {

  /** What the list of subscribed feeds shows */
  sealed interface Content {

    /** The list has not loaded yet */
    data object Loading : Content

    /**
     * The list loaded
     *
     * @property feedUrls Subscribed feed URLs in their stored order
     */
    data class Loaded(val feedUrls: List<String>) : Content

    /**
     * The list could not be loaded
     *
     * @property reason Why loading failed
     */
    data class Error(val reason: FeedLoadError) : Content
  }
}
