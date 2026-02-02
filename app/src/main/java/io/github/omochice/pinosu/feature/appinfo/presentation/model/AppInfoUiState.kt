package io.github.omochice.pinosu.feature.appinfo.presentation.model

/**
 * UI state for the app info screen.
 *
 * @property versionName Application version name from BuildConfig
 * @property commitHash Git commit hash from BuildConfig
 */
data class AppInfoUiState(
    val versionName: String,
    val commitHash: String,
) {
  /** Formatted version text that appends the commit hash when available. */
  val versionDisplayText: String
    get() =
        if (commitHash.isNotEmpty() && commitHash != "unknown") {
          "$versionName ($commitHash)"
        } else {
          versionName
        }
}
