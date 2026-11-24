package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.ui.component.PagerSyncedTabIndicator

/**
 * Tabs that narrow the timeline to one category: a tab showing every feed, then one per category
 *
 * @param categories Category names in the order to show their tabs
 * @param selectedCategory Category whose tab is selected, or null when the tab showing every feed
 *   is
 * @param pagerState State of the pager showing one page per tab, in the order of [feedCategoryTabs]
 * @param onSelect Callback with the category of the tapped tab, or null for the tab showing every
 *   feed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedCategoryTabRow(
    categories: List<String>,
    selectedCategory: String?,
    pagerState: PagerState,
    onSelect: (String?) -> Unit,
) {
  val tabs = feedCategoryTabs(categories)
  PrimaryScrollableTabRow(
      selectedTabIndex = feedCategoryTabIndex(tabs, selectedCategory),
      indicator = { PagerSyncedTabIndicator(pagerState) }) {
        tabs.forEach { category ->
          Tab(
              selected = category == selectedCategory,
              onClick = { onSelect(category) },
              text = { Text(category ?: stringResource(R.string.tab_all_feeds)) })
        }
      }
}

/**
 * Category of each tab in the order shown
 *
 * @param categories Category names in the order to show their tabs
 * @return null for the tab showing every feed, followed by [categories]
 */
internal fun feedCategoryTabs(categories: List<String>): List<String?> = listOf(null) + categories

/**
 * Index of the tab of [category] among [tabs], or of the tab showing every feed when no tab has it
 *
 * @param tabs Tabs from [feedCategoryTabs]
 * @param category Category to look up, or null for the tab showing every feed
 */
internal fun feedCategoryTabIndex(tabs: List<String?>, category: String?): Int =
    tabs.indexOf(category).coerceAtLeast(0)
