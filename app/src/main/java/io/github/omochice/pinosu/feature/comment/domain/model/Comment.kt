package io.github.omochice.pinosu.feature.comment.domain.model

/**
 * NIP-22 kind 1111 comment or synthetic author comment
 *
 * @property id Event ID (or synthetic ID for author comments)
 * @property content Comment text
 * @property authorPubkey Hex-encoded public key of the comment author
 * @property createdAt Unix timestamp in seconds
 * @property isAuthorComment True if this represents the bookmark event's own content rather than a
 *   kind 1111 reply
 */
data class Comment(
    val id: String,
    val content: String,
    val authorPubkey: String,
    val createdAt: Long,
    val isAuthorComment: Boolean,
)
