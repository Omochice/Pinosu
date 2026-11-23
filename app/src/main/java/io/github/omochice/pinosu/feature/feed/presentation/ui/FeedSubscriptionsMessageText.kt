package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.annotation.StringRes
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedSubscriptionsMessage

/** String resource telling the user about this outcome */
@StringRes
internal fun FeedSubscriptionsMessage.messageRes(): Int =
    when (this) {
      FeedSubscriptionsMessage.AlreadySubscribed -> R.string.message_feed_already_subscribed
      FeedSubscriptionsMessage.PartiallyPublished -> R.string.message_feed_list_partially_published
      FeedSubscriptionsMessage.ReadOnly -> R.string.error_feed_read_only
      FeedSubscriptionsMessage.NoUserRelays -> R.string.error_feed_no_user_relays
      FeedSubscriptionsMessage.AccessDenied -> R.string.error_feed_access_denied
      FeedSubscriptionsMessage.Unreachable -> R.string.error_feed_unreachable
      FeedSubscriptionsMessage.NoFeedFound -> R.string.error_feed_not_found
      FeedSubscriptionsMessage.HttpFeedNotSupported -> R.string.error_feed_http_not_supported
      FeedSubscriptionsMessage.SigningCancelled -> R.string.error_signing_cancelled
      FeedSubscriptionsMessage.SignedEventBuildFailed -> R.string.error_signed_event_build_failed
      FeedSubscriptionsMessage.PublishFailed -> R.string.error_feed_list_publish_failed
      FeedSubscriptionsMessage.ChangeFailed -> R.string.error_feed_change_failed
    }
