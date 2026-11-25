package io.github.omochice.pinosu.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

private const val CARD_TITLE_MAX_LINES = 2

/** Title of a bookmark or feed item card, shared so that every card shows its title alike */
@Composable
fun CardTitle(text: String) {
  Text(
      text = text,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.primary,
      maxLines = CARD_TITLE_MAX_LINES,
      overflow = TextOverflow.Ellipsis)
}
