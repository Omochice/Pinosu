package io.github.omochice.pinosu.feature.feed.data.parser

/** Turns a language declared by a feed into a BCP 47 tag */
internal object LanguageTag {

  /**
   * Normalize a declared language
   *
   * Feeds written by hand often use the `en_US` form of Java locales, which a BCP 47 parser would
   * reject, so underscores become hyphens.
   *
   * @param declared Language as written in the feed, if any
   * @return The tag, or null when [declared] is missing or blank
   */
  fun normalize(declared: String?): String? =
      declared?.trim()?.replace('_', '-')?.takeIf { it.isNotEmpty() }
}
