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

/**
 * Bookmark detail screen route
 *
 * @property eventId Event ID of the kind 39701 bookmark
 * @property authorPubkey Hex-encoded public key of the bookmark author
 * @property dTag The d-tag of the bookmark event (URL without scheme)
 * @property title Bookmark title, or null if not available
 * @property content Bookmark event content (author's comment)
 * @property createdAt Unix timestamp of the bookmark event
 * @property urls List of bookmark URLs
 * @property imageUrl OGP image URL, or null if not available
 */
@Serializable
data class BookmarkDetail(
    val eventId: String,
    val authorPubkey: String,
    val dTag: String,
    val title: String? = null,
    val content: String = "",
    val createdAt: Long = 0L,
    val urls: List<String> = emptyList(),
    val imageUrl: String? = null,
) : Route

/** Settings screen route */
@Serializable object Settings : Route
