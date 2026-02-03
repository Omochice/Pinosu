package io.github.omochice.pinosu.feature.comment.domain.model

/**
 * Comment displayed on the bookmark detail screen
 *
 * Represents a NIP-22 kind 1111 comment, a kind 1 text note resolved from a nostr:nevent reference,
 * or a synthetic author comment.
 *
 * @property id Event ID (or synthetic ID for author comments)
 * @property content Comment text
 * @property authorPubkey Hex-encoded public key of the comment author
 * @property createdAt Unix timestamp in seconds
 * @property isAuthorComment True if this represents the bookmark event's own content rather than a
 *   kind 1111 reply
 * @property kind Nostr event kind (e.g., 1 for text note, 1111 for NIP-22 comment)
 */
data class Comment(
    val id: String,
    val content: String,
    val authorPubkey: String,
    val createdAt: Long,
    val isAuthorComment: Boolean,
    val kind: Int = KIND_COMMENT,
) {
  companion object {
    const val KIND_TEXT_NOTE = 1
    const val KIND_COMMENT = 1111
  }
}
