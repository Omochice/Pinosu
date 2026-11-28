package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.ui.component.PagerSyncedTabIndicator

// Tabs keep one width, unlike tabs sized to their names, so the row looks even however long the
// names are; beyond what fits, the list of every category is the way to reach a far tab.
private const val TABS_PER_SCREEN = 2

/**
 * Tabs that narrow the timeline to one category: a tab showing every feed, then one per category
 *
 * @param categories Category names in the order to show their tabs
 * @param selectedCategory Category whose tab is selected, or null for the tab showing every feed
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
  Column {
    BoxWithConstraints {
      val tabWidth = maxWidth / TABS_PER_SCREEN
      Row(verticalAlignment = Alignment.CenterVertically) {
        PrimaryScrollableTabRow(
            selectedTabIndex = feedCategoryTabIndex(tabs, selectedCategory),
            modifier = Modifier.weight(1f),
            edgePadding = 0.dp,
            minTabWidth = tabWidth,
            indicator = { PagerSyncedTabIndicator(pagerState) },
            divider = {}) {
              tabs.forEach { category ->
                Tab(
                    selected = category == selectedCategory,
                    onClick = { onSelect(category) },
                    text = { Text(feedCategoryLabel(category)) })
              }
            }
        if (tabs.size > TABS_PER_SCREEN) {
          FeedCategoryMenu(tabs = tabs, selectedCategory = selectedCategory, onSelect = onSelect)
        }
      }
    }
    HorizontalDivider()
  }
}

@Composable
private fun FeedCategoryMenu(
    tabs: List<String?>,
    selectedCategory: String?,
    onSelect: (String?) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    IconButton(onClick = { expanded = true }) {
      Icon(
          imageVector = Icons.Default.MoreHoriz,
          contentDescription = stringResource(R.string.cd_show_all_categories))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      tabs.forEach { category ->
        DropdownMenuItem(
            text = { Text(feedCategoryLabel(category)) },
            onClick = {
              expanded = false
              onSelect(category)
            },
            trailingIcon =
                if (category == selectedCategory) {
                  { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
                } else {
                  null
                })
      }
    }
  }
}

@Composable
private fun feedCategoryLabel(category: String?): String =
    category ?: stringResource(R.string.tab_all_feeds)

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
