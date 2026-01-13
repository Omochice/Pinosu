package io.github.omochice.pinosu.domain.model

/**
 * Domain model for Nostr kind:1111 comment events
 *
 * Represents a comment attached to another Nostr event (e.g., a bookmark event). Comments reference
 * the target event via the `e` tag in the original Nostr event.
 *
 * @property id Unique identifier of the comment event (32-byte hex-encoded SHA256 hash)
 * @property content Text content of the comment
 * @property author Public key of the comment author (32-byte hex)
 * @property createdAt Unix timestamp in seconds when the comment was created
 * @property referencedEventId Event ID that this comment references via `e` tag
 */
data class Comment(
    val id: String,
    val content: String,
    val author: String,
    val createdAt: Long,
    val referencedEventId: String,
)
