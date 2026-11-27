package io.github.omochice.pinosu.feature.feed.data.local

import kotlinx.serialization.Serializable

/**
 * Article image lookups kept between app runs
 *
 * @property lookups Lookup of each article link
 */
@Serializable
data class ArticleImageLookups(val lookups: Map<String, ArticleImageLookup> = emptyMap())

/**
 * What an article page declared as its image when it was read
 *
 * @property imageUrl Absolute HTTP(S) URL of the image, or null when the page declares none
 * @property lookedUpAtEpochMillis When the page was read, in milliseconds since the epoch
 */
@Serializable data class ArticleImageLookup(val imageUrl: String?, val lookedUpAtEpochMillis: Long)
