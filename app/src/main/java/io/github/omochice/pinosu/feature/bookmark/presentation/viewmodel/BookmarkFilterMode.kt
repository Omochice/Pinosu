package io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel

/**
 * Filter mode for bookmark list display
 *
 * @property Local Shows only the logged-in user's own bookmarks
 * @property Global Shows all bookmarks from the relay
 */
enum class BookmarkFilterMode {
  Local,
  Global
}
