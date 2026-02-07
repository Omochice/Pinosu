package io.github.omochice.pinosu.feature.settings.domain.model

/**
 * Represents supported application locales.
 *
 * @property tag BCP 47 language tag. Empty string means follow system locale.
 */
@Suppress("EnumWrapping")
enum class AppLocale(val tag: String) {
  System(""),
  English("en"),
  Japanese("ja"),
  ;

  companion object {
    /**
     * Resolve an [AppLocale] from a BCP 47 language tag.
     *
     * @param tag BCP 47 language tag, or empty/null for system default
     * @return Matching [AppLocale], or [System] if no match
     */
    fun fromTag(tag: String?): AppLocale = entries.firstOrNull { it.tag == tag } ?: System
  }
}
