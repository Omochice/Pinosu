package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.annotation.StringRes
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedLoadError

/** String resource explaining this reason to the user */
@StringRes
internal fun FeedLoadError.messageRes(): Int =
    when (this) {
      FeedLoadError.NoUserRelays -> R.string.error_feed_no_user_relays
      FeedLoadError.AccessDenied -> R.string.error_feed_access_denied
      FeedLoadError.Unreachable -> R.string.error_feed_unreachable
      FeedLoadError.Unknown -> R.string.error_feed_load_failed
    }
