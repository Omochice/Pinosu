package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.omochice.pinosu.R

/**
 * Tabs that narrow the timeline to one category: a tab showing every feed, then one per category
 *
 * Unlike the bookmark tabs this row has no pager, because every tab filters the one timeline that
 * is already loaded and the number of tabs follows the user's categories.
 *
 * @param categories Category names in the order to show their tabs
 * @param selectedCategory Category whose tab is selected, or null when the tab showing every feed
 *   is
 * @param onSelect Callback with the category of the tapped tab, or null for the tab showing every
 *   feed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedCategoryTabRow(
    categories: List<String>,
    selectedCategory: String?,
    onSelect: (String?) -> Unit,
) {
  val tabs = listOf(null) + categories
  PrimaryScrollableTabRow(selectedTabIndex = tabs.indexOf(selectedCategory).coerceAtLeast(0)) {
    tabs.forEach { category ->
      Tab(
          selected = category == selectedCategory,
          onClick = { onSelect(category) },
          text = { Text(category ?: stringResource(R.string.tab_all_feeds)) })
    }
  }
}
