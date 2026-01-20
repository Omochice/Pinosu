package io.github.omochice.pinosu.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route

/** Login screen route */
@Serializable object Login : Route

/** Main screen route */
@Serializable object Main : Route

/** Bookmark list screen route */
@Serializable object Bookmark : Route

/** License screen route */
@Serializable object License : Route

/** App info screen route */
@Serializable object AppInfo : Route

/**
 * Post bookmark screen route
 *
 * @property sharedUrl URL shared from other apps, or null if not from share intent
 * @property sharedComment Non-URL text shared from other apps, or null if not from share intent
 */
@Serializable
data class PostBookmark(val sharedUrl: String? = null, val sharedComment: String? = null) : Route
