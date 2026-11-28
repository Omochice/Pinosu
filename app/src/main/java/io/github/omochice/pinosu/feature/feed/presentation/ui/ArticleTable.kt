package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val GapBelowCaption = 4.dp
private val CellPadding = 8.dp

/**
 * Table of an article, with its caption above a grid that scrolls sideways when it is wider than
 * the screen
 *
 * @param table Table with the styled text of its caption and cells
 * @param style Style of the text, which header cells additionally bold
 * @param modifier Modifier for the table
 */
@Composable
internal fun ArticleTable(
    table: Table<AnnotatedString>,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
  val columnSpans = remember(table) { table.rows.map { row -> row.map { it.columnSpan } } }
  Column(modifier = modifier) {
    if (table.caption != null) {
      Text(text = table.caption, style = style)
      Spacer(modifier = Modifier.height(GapBelowCaption))
    }
    TableGrid(
        columnSpans = columnSpans,
        modifier =
            Modifier.horizontalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.outlineVariant)) {
          table.rows.forEach { row -> row.forEach { cell -> ArticleTableCell(cell, style) } }
        }
  }
}

@Composable
private fun ArticleTableCell(cell: TableCell<AnnotatedString>, style: TextStyle) {
  val colors = MaterialTheme.colorScheme
  Box(
      modifier =
          Modifier.background(if (cell.isHeader) colors.surfaceVariant else colors.background)
              .padding(CellPadding)) {
        Text(
            text = cell.content,
            style = if (cell.isHeader) style.copy(fontWeight = FontWeight.Bold) else style)
      }
}
