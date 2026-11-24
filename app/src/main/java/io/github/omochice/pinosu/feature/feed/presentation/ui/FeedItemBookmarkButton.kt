package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.omochice.pinosu.R

/** Action that bookmarks a feed item */
@Composable
internal fun FeedItemBookmarkButton(onClick: () -> Unit) {
  IconButton(onClick = onClick) {
    Icon(
        imageVector = Icons.Outlined.BookmarkAdd,
        contentDescription = stringResource(R.string.cd_bookmark_feed_item))
  }
}
