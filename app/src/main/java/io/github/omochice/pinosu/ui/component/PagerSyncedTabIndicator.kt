package io.github.omochice.pinosu.ui.component

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Indicator of a tab row whose tabs are the pages of [pagerState], following the pager while the
 * user swipes between pages
 *
 * The default tabIndicatorOffset animates only after the pager settles; deriving the position from
 * the pager's scroll offset keeps the indicator under the finger during a swipe.
 *
 * @param pagerState State of the pager whose page each tab shows, one tab per page in order
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabIndicatorScope.PagerSyncedTabIndicator(pagerState: PagerState) {
  TabRowDefaults.PrimaryIndicator(
      modifier =
          Modifier.tabIndicatorLayout { measurable, constraints, tabPositions ->
                // The default tabIndicatorOffset guards against an empty position list, so mirror
                // it here rather than let coerceIn throw on an empty index range.
                if (tabPositions.isEmpty()) {
                  layout(0, 0) {}
                } else {
                  val progress = pagerState.currentPage + pagerState.currentPageOffsetFraction
                  val fromTab =
                      tabPositions[floor(progress).toInt().coerceIn(0, tabPositions.lastIndex)]
                  val toTab =
                      tabPositions[ceil(progress).toInt().coerceIn(0, tabPositions.lastIndex)]
                  val fraction = progress - floor(progress)
                  val indicatorWidth = lerp(fromTab.contentWidth, toTab.contentWidth, fraction)
                  val indicatorCenter =
                      lerp(fromTab.left + fromTab.width / 2, toTab.left + toTab.width / 2, fraction)
                  val widthPx = indicatorWidth.roundToPx()
                  val placeable =
                      measurable.measure(constraints.copy(minWidth = widthPx, maxWidth = widthPx))
                  // The tab row bottom-aligns the indicator using the reported height, so the
                  // layout must not stretch to the full row height.
                  layout(constraints.maxWidth, placeable.height) {
                    // Where the row puts the indicator differs between fixed and scrollable rows
                    // and is not part of their contract, so read it instead of assuming it.
                    val slotLeft = coordinates?.positionInParent()?.x?.roundToInt() ?: 0
                    placeable.place(indicatorCenter.roundToPx() - widthPx / 2 - slotLeft, 0)
                  }
                }
              }
              .testTag("tabIndicator"),
      width = Dp.Unspecified)
}
