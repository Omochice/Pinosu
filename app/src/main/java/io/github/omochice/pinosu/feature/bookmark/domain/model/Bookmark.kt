package io.github.omochice.pinosu.feature.bookmark.domain.model

/**
 * Bookmark item from kind:39701
 *
 * @property type Type of bookmark (event for event-based display)
 * @property eventId Event ID (kind 39701 event ID)
 * @property url URL reference (first r tag URL)
 * @property title Title (from title tag or og:title)
 * @property imageUrl OGP image URL (from og:image)
 * @property event Full event data (contains content as description)
 * @property urls List of all r tag URLs in the event
 * @property rawJson Serialized NostrEvent JSON for clipboard copy
 */
data class BookmarkItem(
    val type: String,
    val eventId: String? = null,
    val url: String? = null,
    val title: String? = null,
    val imageUrl: String? = null,
    val event: BookmarkedEvent? = null,
    val urls: List<String> = emptyList(),
    val rawJson: String? = null,
)

/**
 * Bookmarked event details
 *
 * @property kind Event kind
 * @property content Event content
 * @property author Author's public key
 * @property createdAt Unix timestamp
 * @property tags Event tags (for NIP-10 reply parsing)
 */
data class BookmarkedEvent(
    val kind: Int,
    val content: String,
    val author: String,
    val createdAt: Long,
    val tags: List<List<String>> = emptyList(),
)

/**
 * Bookmark list (kind:39701)
 *
 * @property pubkey Author's public key
 * @property items List of bookmarked events
 * @property createdAt Unix timestamp
 * @property encryptedContent Encrypted content (NIP-04) if present
 */
data class BookmarkList(
    val pubkey: String,
    val items: List<BookmarkItem>,
    val createdAt: Long,
    val encryptedContent: String? = null,
)
