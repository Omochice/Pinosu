package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

/**
 * Order category names for showing them to the user, as timeline tabs or as suggestions
 *
 * Names that differ only in case stay separate, in the order they were given.
 *
 * @param categories Category names of the subscribed feeds
 * @return Each name once, in name order ignoring case
 */
internal fun orderedCategories(categories: List<String>): List<String> =
    categories.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)

/**
 * What the tab of [category] shows of this content
 *
 * @param category Selected category, or null for the tab that shows every feed
 */
internal fun FeedUiState.Content.inCategory(category: String?): FeedUiState.Content =
    if (category == null || this !is FeedUiState.Content.Loaded) {
      this
    } else {
      FeedUiState.Content.Loaded(
          items = items.filter { it.feedCategory == category },
          failedFeeds = failedFeeds.filter { it.feedCategory == category })
    }
