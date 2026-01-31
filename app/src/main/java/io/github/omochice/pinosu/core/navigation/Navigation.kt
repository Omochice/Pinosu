package io.github.omochice.pinosu.core.navigation

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
 * @property sharedUrl Pre-filled URL from share intent, or null
 * @property sharedComment Pre-filled comment from share intent, or null
 */
@Serializable
data class PostBookmark(val sharedUrl: String? = null, val sharedComment: String? = null) : Route

/** Settings screen route */
@Serializable object Settings : Route
