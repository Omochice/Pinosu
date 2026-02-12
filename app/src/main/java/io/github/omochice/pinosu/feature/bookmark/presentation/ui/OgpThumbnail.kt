package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * OGP thumbnail image or "NO IMAGE" placeholder
 *
 * @param imageUrl OGP image URL, or null to show a gray placeholder
 * @param contentDescription Accessibility description for the image
 * @param modifier Modifier for layout customization
 */
@Composable
fun OgpThumbnail(imageUrl: String?, contentDescription: String?, modifier: Modifier = Modifier) {
  val shape = RoundedCornerShape(8.dp)
  if (imageUrl != null) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier.clip(shape),
        contentScale = ContentScale.Crop)
  } else {
    Box(
        modifier = modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center) {
          Text(
              text = "NO IMAGE",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
  }
}
