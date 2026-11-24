package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier

/**
 * State of a pager with one page per category tab, opening on the page of [selectedCategory]
 *
 * A different set of categories gets a new state, because the pages of the old one no longer match
 * the tabs, and clamping its page to the new count would select whatever tab happens to land there.
 *
 * @param categories Category names in the order to show their tabs
 * @param selectedCategory Category whose tab is selected, or null for the tab showing every feed
 * @return The state, or null when there are no categories and so no tabs to page through
 */
@Composable
internal fun rememberFeedCategoryPagerState(
    categories: List<String>,
    selectedCategory: String?,
): PagerState? =
    if (categories.isEmpty()) {
      null
    } else {
      key(categories) {
        val tabs = feedCategoryTabs(categories)
        rememberPagerState(
            initialPage = feedCategoryTabIndex(tabs, selectedCategory), pageCount = { tabs.size })
      }
    }

/**
 * Pager showing one page per category tab, kept in step with the selected tab both ways
 *
 * The pager composes pages that are not on screen, such as the next page ahead of a swipe, so each
 * page is told whether it is in the viewport, letting its cards look up article images only once
 * the user can see them.
 *
 * @param pagerState State from [rememberFeedCategoryPagerState] for the same [categories]
 * @param categories Category names in the order to show their tabs
 * @param selectedCategory Category whose tab is selected, or null for the tab showing every feed
 * @param onSelect Callback with the category of the page the user swiped to, or null for the page
 *   showing every feed
 * @param page Content of the page of a category, or of the page showing every feed for null, and
 *   whether that page is in the viewport
 */
@Composable
internal fun FeedCategoryPager(
    pagerState: PagerState,
    categories: List<String>,
    selectedCategory: String?,
    onSelect: (String?) -> Unit,
    page: @Composable (category: String?, isShown: Boolean) -> Unit,
) {
  val tabs = remember(categories) { feedCategoryTabs(categories) }
  val selectedPage = feedCategoryTabIndex(tabs, selectedCategory)
  LaunchedEffect(pagerState, selectedPage) {
    if (pagerState.currentPage != selectedPage) {
      pagerState.animateScrollToPage(selectedPage)
    }
  }

  val currentTabs by rememberUpdatedState(tabs)
  val currentSelected by rememberUpdatedState(selectedCategory)
  val currentOnSelect by rememberUpdatedState(onSelect)
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }
        .collect { settled ->
          val category = currentTabs[settled]
          if (category != currentSelected) currentOnSelect(category)
        }
  }

  HorizontalPager(
      state = pagerState, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 0) { index ->
        val isShown by
            remember(pagerState, index) {
              derivedStateOf { pagerState.layoutInfo.visiblePagesInfo.any { it.index == index } }
            }
        page(tabs[index], isShown)
      }
}
