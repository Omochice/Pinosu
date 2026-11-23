package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp

private val MinColumnWidth = 48.dp
private val MaxColumnWidth = 240.dp
private val GridLineWidth = 1.dp

/**
 * Grid of cells where every column is as wide as the widest of its cells, within limits, and every
 * row is as tall as the tallest of its cells
 *
 * Cells are measured to fill the whole of their place, and the lines between and around them are
 * left empty, so the background of [modifier] shows through as the borders of the grid.
 *
 * @param columnSpans Number of columns each cell covers, row by row, where every row covers the
 *   same number of columns
 * @param modifier Modifier for the grid
 * @param content Cells, row by row from left to right, one for each entry of [columnSpans]
 */
@Composable
internal fun TableGrid(
    columnSpans: List<List<Int>>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
  val measurePolicy = remember(columnSpans) { TableGridMeasurePolicy(gridSlotsOf(columnSpans)) }
  Layout(content = content, modifier = modifier, measurePolicy = measurePolicy)
}

private data class GridSlot(val row: Int, val column: Int, val span: Int) {
  val columns: IntRange
    get() = column until column + span
}

private fun gridSlotsOf(columnSpans: List<List<Int>>): List<GridSlot> =
    columnSpans.flatMapIndexed { row, spans ->
      spans.runningFold(0, Int::plus).zip(spans) { column, span -> GridSlot(row, column, span) }
    }

/**
 * Sizes are taken from the intrinsic measurements of the cells, because a cell can be measured only
 * once and has to be measured at the full height of its row for its background to fill the row.
 */
private class TableGridMeasurePolicy(private val slots: List<GridSlot>) : MeasurePolicy {
  private val columnCount = slots.maxOfOrNull { it.column + it.span } ?: 0
  private val rowCount = slots.maxOfOrNull { it.row + 1 } ?: 0

  override fun MeasureScope.measure(
      measurables: List<Measurable>,
      constraints: Constraints,
  ): MeasureResult {
    val cells = slots.zip(measurables)
    val line = GridLineWidth.roundToPx()
    val limits = MinColumnWidth.roundToPx()..MaxColumnWidth.roundToPx()
    val columns = GridTracks(columnWidthsOf(cells, limits, line), line)
    val rows = GridTracks(rowHeightsOf(cells, columns), line)
    val placeables =
        cells.map { (slot, cell) ->
          cell.measure(Constraints.fixed(columns.sizeOf(slot.columns), rows.sizeOf(slot.row)))
        }
    return layout(
        constraints.constrainWidth(columns.total), constraints.constrainHeight(rows.total)) {
          slots.zip(placeables).forEach { (slot, placeable) ->
            placeable.place(columns.startOf(slot.column), rows.startOf(slot.row))
          }
        }
  }

  /**
   * A cell spanning columns is fitted only after the cells of single columns have set the widths,
   * because its own width does not tell how to divide it among the columns it covers.
   */
  private fun columnWidthsOf(
      cells: List<Pair<GridSlot, Measurable>>,
      limits: IntRange,
      line: Int,
  ): IntArray {
    val widths = IntArray(columnCount) { limits.first }
    val (single, spanning) = cells.partition { (slot) -> slot.span == 1 }
    single.forEach { (slot, cell) ->
      widths[slot.column] = maxOf(widths[slot.column], cell.preferredWidth(limits))
    }
    spanning.forEach { (slot, cell) ->
      val available = slot.columns.sumOf { widths[it] } + line * (slot.span - 1)
      val needed = cell.preferredWidth(limits.first..limits.last * slot.span)
      val share = (needed - available + slot.span - 1) / slot.span
      if (share > 0) {
        slot.columns.forEach { widths[it] += share }
      }
    }
    return widths
  }

  private fun rowHeightsOf(cells: List<Pair<GridSlot, Measurable>>, columns: GridTracks): IntArray {
    val heights = IntArray(rowCount)
    cells.forEach { (slot, cell) ->
      heights[slot.row] =
          maxOf(heights[slot.row], cell.minIntrinsicHeight(columns.sizeOf(slot.columns)))
    }
    return heights
  }

  private fun Measurable.preferredWidth(limits: IntRange): Int =
      maxIntrinsicWidth(Constraints.Infinity).coerceIn(limits)
}

/**
 * Columns or rows of a grid with a line before, between, and after them
 *
 * @param sizes Size of each column or row, without lines
 * @param line Width of a line
 */
private class GridTracks(sizes: IntArray, private val line: Int) {
  private val starts = sizes.runningFold(line) { start, size -> start + size + line }

  /** Size of the whole grid along this axis, lines included */
  val total: Int
    get() = starts.last()

  fun startOf(index: Int): Int = starts[index]

  /** Size of a cell covering [indices], with the lines between them */
  fun sizeOf(indices: IntRange): Int = starts[indices.last + 1] - line - starts[indices.first]

  fun sizeOf(index: Int): Int = sizeOf(index..index)
}
