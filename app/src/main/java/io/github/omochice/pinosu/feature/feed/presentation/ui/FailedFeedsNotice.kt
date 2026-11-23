package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure

/**
 * Notice listing the subscribed feeds that could not be fetched
 *
 * @param failedFeeds Feeds that could not be fetched, in subscription order
 */
@Composable
fun FailedFeedsNotice(failedFeeds: List<FeedFetchFailure>) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer,
              contentColor = MaterialTheme.colorScheme.onErrorContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              text = stringResource(R.string.message_feeds_partially_failed),
              style = MaterialTheme.typography.titleSmall)
          failedFeeds.forEach { failure ->
            Text(
                text = failure.feedUrl,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
          }
        }
      }
}
