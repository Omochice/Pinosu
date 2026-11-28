package io.github.omochice.pinosu.feature.feed.presentation.ui

/**
 * Grouped callbacks for the feed subscription screen
 *
 * @property onNavigateUp Callback when the back button is tapped
 * @property onRetry Callback when loading the list is retried
 * @property onAdd Callback with the URL the user entered or picked to subscribe to
 * @property onRemove Callback with the URL of the feed whose remove button is tapped
 * @property onSetCategory Callback with the URL of a feed and the category the user filed it under,
 *   or null when the user took it out of its category
 * @property onConfirmPublicNotice Callback when the user agrees that the list is public
 * @property onDismissDialog Callback when the user closes a dialog without answering it
 * @property onMessageShown Callback once the current message has been shown
 */
data class FeedSubscriptionsCallbacks(
    val onNavigateUp: () -> Unit,
    val onRetry: () -> Unit,
    val onAdd: (String) -> Unit,
    val onRemove: (String) -> Unit,
    val onSetCategory: (feedUrl: String, category: String?) -> Unit,
    val onConfirmPublicNotice: () -> Unit,
    val onDismissDialog: () -> Unit,
    val onMessageShown: () -> Unit,
)
