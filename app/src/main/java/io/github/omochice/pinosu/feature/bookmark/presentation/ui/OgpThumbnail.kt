package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * OGP thumbnail image or placeholder spacer
 *
 * @param imageUrl OGP image URL, or null to show an 80dp placeholder
 * @param modifier Modifier for layout customization
 */
@Composable
fun OgpThumbnail(imageUrl: String?, modifier: Modifier = Modifier) {
  if (imageUrl != null) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop)
  } else {
    Spacer(modifier = modifier.size(80.dp))
  }
}
