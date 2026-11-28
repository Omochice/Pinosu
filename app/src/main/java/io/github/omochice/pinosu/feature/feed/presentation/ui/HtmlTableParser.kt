package io.github.omochice.pinosu.feature.feed.presentation.ui

import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

private val ROW_GROUP_TAGS = setOf("thead", "tbody", "tfoot")
private val CELL_TAGS = setOf("th", "td")

/**
 * Read a `table` element into a [Table] of the inner HTML of its caption and cells
 *
 * A cell spanning rows leaves an empty cell of the same kind in its columns of the rows below, and
 * rows are padded with empty cells, so that every row covers the same columns.
 *
 * A table inside a cell is reduced to its text.
 *
 * @return The table, with rows that have no cells left out
 */
internal fun parseHtmlTable(table: Element): Table<String> {
  val rows = expandRowSpans(rowsOf(table)).filter { it.isNotEmpty() }
  val columnCount = rows.maxOfOrNull { it.columnCount() } ?: 0
  return Table(
      caption = captionOf(table)?.takeIf { it.hasText() }?.let(::inlineHtmlOf),
      rows = rows.map { it.paddedTo(columnCount) })
}

/** The only cell of a table that has a single row with a single cell and no caption */
internal fun soleCellOf(table: Element): Element? =
    rowsOf(table).singleOrNull()?.singleOrNull()?.takeIf { captionOf(table) == null }

private fun captionOf(table: Element): Element? =
    table.children().firstOrNull { it.normalName() == "caption" }

private fun rowsOf(table: Element): List<List<Element>> =
    table
        .children()
        .flatMap { if (it.normalName() in ROW_GROUP_TAGS) it.children() else listOf(it) }
        .filter { it.normalName() == "tr" }
        .map { row -> row.children().filter { it.normalName() in CELL_TAGS } }

private fun expandRowSpans(rows: List<List<Element>>): List<List<TableCell<String>>> {
  var spansFromAbove = emptyMap<Int, RowSpan>()
  return rows.map { cells ->
    val row = ExpandedRow(spansFromAbove)
    cells.forEach(row::add)
    spansFromAbove = row.spansBelow()
    row.finish()
  }
}

/**
 * Cell spanning rows as it is carried into the rows below its own
 *
 * @property placeholder Cell to put in its place in those rows
 * @property rowsLeft Number of rows below that it still covers
 */
private data class RowSpan(val placeholder: TableCell<String>, val rowsLeft: Int)

/** One row being filled, with the cells spanning rows from above put before its own cells */
private class ExpandedRow(private val spansFromAbove: Map<Int, RowSpan>) {
  private val cells = mutableListOf<TableCell<String>>()
  private val newSpans = mutableMapOf<Int, RowSpan>()
  private var column = 0

  fun add(element: Element) {
    putSpansFromAbove()
    val cell =
        TableCell(
            content = inlineHtmlOf(element),
            isHeader = element.normalName() == "th",
            columnSpan = element.span("colspan"))
    val rowSpan = element.span("rowspan")
    if (rowSpan > 1) {
      newSpans[column] = RowSpan(cell.copy(content = ""), rowsLeft = rowSpan - 1)
    }
    put(cell)
  }

  fun finish(): List<TableCell<String>> {
    putSpansFromAbove()
    return cells.toList()
  }

  fun spansBelow(): Map<Int, RowSpan> =
      spansFromAbove
          .filterValues { it.rowsLeft > 1 }
          .mapValues { (_, span) -> span.copy(rowsLeft = span.rowsLeft - 1) } + newSpans

  private fun putSpansFromAbove() {
    var span = spansFromAbove[column]
    while (span != null) {
      put(span.placeholder)
      span = spansFromAbove[column]
    }
  }

  private fun put(cell: TableCell<String>) {
    cells += cell
    column += cell.columnSpan
  }
}

private fun Element.span(attribute: String): Int =
    attr(attribute).trim().toIntOrNull()?.takeIf { it > 0 } ?: 1

/**
 * Images are removed because a cell is laid out as styled text, where an image would only leave a
 * placeholder character.
 */
private fun inlineHtmlOf(element: Element): String {
  val copy = element.clone()
  copy.select("img").remove()
  generateSequence { copy.selectFirst("table") }
      .forEach { nested -> nested.replaceWith(TextNode(nested.text())) }
  return copy.html()
}

private fun List<TableCell<String>>.columnCount(): Int = sumOf { it.columnSpan }

private fun List<TableCell<String>>.paddedTo(columnCount: Int): List<TableCell<String>> =
    this + List(columnCount - columnCount()) { TableCell("", isHeader = false) }
