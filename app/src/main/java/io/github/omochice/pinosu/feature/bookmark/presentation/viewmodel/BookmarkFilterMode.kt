package io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel

/** Filter mode for bookmark list display */
enum class BookmarkFilterMode {
  /** Shows only the logged-in user's own bookmarks */
  Local,

  /** Shows all bookmarks from the relay */
  Global
}
