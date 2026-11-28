package io.github.omochice.pinosu.feature.feed.presentation.ui

/**
 * Table laid out as a grid where the spans of every row add up to the same number of columns
 *
 * @param T Type of the content of the caption and cells
 * @property caption Content of the caption, or null when the table has none
 * @property rows Cells of each row, from left to right
 */
internal data class Table<T>(val caption: T?, val rows: List<List<TableCell<T>>>) {

  /** Table with the same layout whose caption and cells hold [transform] of their content */
  fun <R> map(transform: (T) -> R): Table<R> =
      Table(
          caption = caption?.let(transform),
          rows = rows.map { row -> row.map { it.map(transform) } })
}

/**
 * One cell of a [Table]
 *
 * @param T Type of the content
 * @property content Content of the cell
 * @property isHeader Whether the cell heads a row or a column
 * @property columnSpan Number of columns the cell covers, at least 1
 */
internal data class TableCell<T>(val content: T, val isHeader: Boolean, val columnSpan: Int = 1) {

  /** Cell at the same place holding [transform] of its content */
  fun <R> map(transform: (T) -> R): TableCell<R> =
      TableCell(transform(content), isHeader, columnSpan)
}
