package io.github.omochice.pinosu.presentation.ui.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Drawer menu item component
 *
 * Displays a clickable menu item with an icon and text.
 *
 * @param icon Icon to display
 * @param text Text label for the menu item
 * @param onClick Callback when the item is clicked
 * @param modifier Modifier for this composable
 * @param enabled Whether the menu item is enabled and clickable
 */
@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
  val contentColor =
      if (enabled) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
      }

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .clickable(enabled = enabled, onClick = onClick)
              .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = text, tint = contentColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = contentColor)
      }
}

@Preview(showBackground = true)
@Composable
private fun DrawerMenuItemPreview() {
  DrawerMenuItem(icon = Icons.Default.Info, text = "アプリ情報", onClick = {})
}
