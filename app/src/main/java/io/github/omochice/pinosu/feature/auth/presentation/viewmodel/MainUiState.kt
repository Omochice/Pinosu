package io.github.omochice.pinosu.feature.auth.presentation.viewmodel

/**
 * Main screen UI state
 *
 * @property userPubkey Logged-in user's public key
 * @property isLoggingOut Whether logout process is in progress
 */
data class MainUiState(
    val userPubkey: String? = null,
    val isLoggingOut: Boolean = false,
)
