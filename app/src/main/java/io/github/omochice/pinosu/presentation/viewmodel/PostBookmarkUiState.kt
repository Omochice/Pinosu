package io.github.omochice.pinosu.presentation.viewmodel

/**
 * UI state for post bookmark screen
 *
 * @property url URL without scheme (user input)
 * @property title Bookmark title
 * @property categories Comma-separated categories string
 * @property comment Bookmark comment
 * @property isSubmitting Whether the form is being submitted
 * @property errorMessage Error message to display
 * @property postSuccess Whether the post was successful
 * @property unsignedEventJson Unsigned event JSON for signing (internal state)
 */
data class PostBookmarkUiState(
    val url: String = "",
    val title: String = "",
    val categories: String = "",
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val postSuccess: Boolean = false,
    val unsignedEventJson: String? = null
)
