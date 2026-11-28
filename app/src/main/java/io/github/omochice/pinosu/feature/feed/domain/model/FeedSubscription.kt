package io.github.omochice.pinosu.feature.feed.domain.model

/**
 * One feed in the user's subscription list
 *
 * @property url Feed URL, as subscribed
 * @property category Name of the one category the user filed the feed under, or null when the feed
 *   is in none
 */
data class FeedSubscription(val url: String, val category: String? = null) {

  companion object {
    /**
     * Bring a category name the user entered or a relay returned into its stored form
     *
     * @return [name] without surrounding whitespace, or null when it is null or blank
     */
    fun normalizeCategory(name: String?): String? = name?.trim()?.ifEmpty { null }
  }
}
