package io.github.omochice.pinosu.feature.comment.presentation.ui

/**
 * Bookmark metadata displayed on the detail screen
 *
 * @property title Bookmark title, or null if unavailable
 * @property urls List of bookmark URLs
 * @property createdAt Bookmark creation timestamp (Unix epoch seconds)
 * @property imageUrl OGP image URL, or null if not available
 */
data class BookmarkInfo(
    val title: String?,
    val urls: List<String>,
    val createdAt: Long,
    val imageUrl: String? = null,
)
