package io.github.omochice.pinosu.domain.model

/**
 * Bookmark item from kind:39701
 *
 * @property type Type of bookmark (e, a, r, t, q, p, d, title)
 * @property eventId Event ID (for e/q tag)
 * @property articleCoordinate Article coordinate (for a tag, format: kind:pubkey:d-tag)
 * @property url URL reference (for r tag)
 * @property hashtag Hashtag (for t tag)
 * @property pubkey Public key reference (for p tag)
 * @property identifier Identifier (for d tag)
 * @property title Title (for title tag in kind 39701)
 * @property relayUrl Relay URL hint (optional)
 * @property event Full event data if fetched (optional)
 * @property threadReplies Thread replies for e/q type bookmarks (optional)
 */
data class BookmarkItem(
    val type: String,
    val eventId: String? = null,
    val articleCoordinate: String? = null,
    val url: String? = null,
    val hashtag: String? = null,
    val pubkey: String? = null,
    val identifier: String? = null,
    val title: String? = null,
    val relayUrl: String? = null,
    val event: BookmarkedEvent? = null,
    val threadReplies: List<ThreadReply> = emptyList(),
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
 * Thread reply information
 *
 * @property eventId Event ID of the reply
 * @property event Full reply event data
 * @property depth Thread depth (0 = root bookmark, 1 = direct reply, 2+ = nested)
 * @property replies Nested replies (limited to max depth)
 */
data class ThreadReply(
    val eventId: String,
    val event: BookmarkedEvent? = null,
    val depth: Int,
    val replies: List<ThreadReply> = emptyList(),
)

/**
 * Bookmark list (kind:39701)
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
