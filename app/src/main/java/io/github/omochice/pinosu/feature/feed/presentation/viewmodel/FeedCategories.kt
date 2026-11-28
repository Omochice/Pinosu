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
