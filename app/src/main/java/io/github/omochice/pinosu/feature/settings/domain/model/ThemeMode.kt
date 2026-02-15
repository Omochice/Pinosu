package io.github.omochice.pinosu.feature.settings.domain.model

/**
 * Theme mode for the application appearance.
 *
 * Determines whether the app uses light, dark, or system-default color scheme.
 */
enum class ThemeMode {
  /** Follow the system dark mode setting. This is the default theme mode. */
  System,

  /** Always use light color scheme. */
  Light,

  /** Always use dark color scheme. */
  Dark,
}
