package io.github.omochice.pinosu.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Body of a list card: an [OgpThumbnail] at the start followed by [content]
 *
 * @param imageUrl Thumbnail image URL, or null to show the placeholder
 * @param contentDescription Accessibility description for the thumbnail
 * @param content What follows the thumbnail; give the text a weight so it takes the remaining width
 */
@Composable
fun ThumbnailRow(
    imageUrl: String?,
    contentDescription: String?,
    content: @Composable RowScope.() -> Unit,
) {
  Row(modifier = Modifier.padding(16.dp)) {
    OgpThumbnail(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = Modifier.size(80.dp))
    Spacer(modifier = Modifier.width(12.dp))
    content()
  }
}
