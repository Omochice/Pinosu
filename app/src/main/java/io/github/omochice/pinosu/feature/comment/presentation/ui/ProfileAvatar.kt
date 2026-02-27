package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Circular profile avatar with fallback to a default person icon
 *
 * @param imageUrl Profile image URL, or null for default icon
 * @param contentDescription Accessibility description
 * @param size Avatar diameter
 */
@Composable
internal fun ProfileAvatar(
    imageUrl: String?,
    contentDescription: String?,
    size: Dp = 32.dp,
) {
  if (imageUrl != null) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = Modifier.size(size).clip(CircleShape),
        contentScale = ContentScale.Crop,
    )
  } else {
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = contentDescription,
        modifier =
            Modifier.size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
