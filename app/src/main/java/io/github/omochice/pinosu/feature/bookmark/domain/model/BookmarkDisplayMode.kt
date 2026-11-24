package io.github.omochice.pinosu.feature.bookmark.domain.model

/**
 * Display mode for the bookmark list and the feed timeline.
 *
 * Determines whether items are displayed as a vertical list or a staggered grid.
 */
enum class BookmarkDisplayMode {
  /** Vertical list layout using LazyColumn. This is the default display mode. */
  List,

  /** Staggered grid layout using LazyVerticalStaggeredGrid for Pinterest-style display. */
  Grid,
}
