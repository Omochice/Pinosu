package io.github.omochice.pinosu.domain.model

/**
 * Bookmark item from NIP-51 kind:10003
 *
 * @property type Type of bookmark (e, a, r, t)
 * @property eventId Event ID (for e tag)
 * @property articleCoordinate Article coordinate (for a tag, format: kind:pubkey:d-tag)
 * @property url URL reference (for r tag)
 * @property hashtag Hashtag (for t tag)
 * @property relayUrl Relay URL hint (optional)
 * @property event Full event data if fetched (optional)
 */
data class BookmarkItem(
    val type: String, // "e", "a", "r", "t"
    val eventId: String? = null,
    val articleCoordinate: String? = null,
    val url: String? = null,
    val hashtag: String? = null,
    val relayUrl: String? = null,
    val event: BookmarkedEvent? = null,
)

/**
 * Bookmarked event details
 *
 * @property kind Event kind
 * @property content Event content
 * @property author Author's public key
 * @property createdAt Unix timestamp
 */
data class BookmarkedEvent(
    val kind: Int,
    val content: String,
    val author: String,
    val createdAt: Long,
)

/**
 * Bookmark list (kind:10003)
 *
 * @property pubkey Author's public key
 * @property items List of bookmarked events
 * @property createdAt Unix timestamp
 * @property rawEventJson Raw event JSON for debugging (optional)
 * @property encryptedContent Encrypted content (NIP-04) if present
 * @property decryptedContent Decrypted content if available
 */
data class BookmarkList(
    val pubkey: String,
    val items: List<BookmarkItem>,
    val createdAt: Long,
    val rawEventJson: String? = null,
    val encryptedContent: String? = null,
    val decryptedContent: String? = null,
)
