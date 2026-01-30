package io.github.omochice.pinosu.feature.shareintent.domain.model

/**
 * Represents content received from an external app via ACTION_SEND intent
 *
 * @property url Shared URL, or null if the shared text was not a URL
 * @property comment Shared non-URL text, or null if the shared text was a URL
 */
data class SharedContent(val url: String? = null, val comment: String? = null)
