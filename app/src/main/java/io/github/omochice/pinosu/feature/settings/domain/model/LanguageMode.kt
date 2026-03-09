package io.github.omochice.pinosu.feature.settings.domain.model

/**
 * Language mode for the application UI.
 *
 * Determines whether the app uses English, Japanese, or system-default language.
 */
enum class LanguageMode {
  /** Follow the system language setting. This is the default language mode. */
  System,

  /** Always use English. */
  English,

  /** Always use Japanese. */
  Japanese,
}
