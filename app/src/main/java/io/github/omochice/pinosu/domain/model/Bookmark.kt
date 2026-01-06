package io.github.omochice.pinosu.domain.model

/**
 * Bookmark item from kind:39701
 *
 * @property type Type of bookmark (event for event-based display)
 * @property eventId Event ID (kind 39701 event ID)
 * @property articleCoordinate Article coordinate (deprecated, for backward compatibility)
 * @property url URL reference (first r tag URL, for backward compatibility)
 * @property hashtag Hashtag (deprecated, for backward compatibility)
 * @property pubkey Public key reference (deprecated, for backward compatibility)
 * @property identifier Identifier (deprecated, for backward compatibility)
 * @property title Title (from title tag or og:title)
 * @property relayUrl Relay URL hint (optional)
 * @property event Full event data (contains content as description)
 * @property threadReplies Thread replies (deprecated, not used for r tags)
 * @property urls List of all r tag URLs in the event
 * @property titleSource Source of title ("tag" or "metadata")
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
    val urls: List<String> = emptyList(),
    val titleSource: String = "tag",
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
