package io.github.omochice.pinosu.feature.shareintent.domain.model

/**
 * Represents content received from an external app via ACTION_SEND intent
 *
 * @property url First URL found in the shared text, or null if none
 * @property comment Text surrounding the URL (URL removed), or the full text if no URL was found
 */
data class SharedContent(val url: String? = null, val comment: String? = null)
