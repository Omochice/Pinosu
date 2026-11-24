package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.core.navigation.FeedItemReader
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * ViewModel for the feed item reader screen
 *
 * The item is looked up once, so a timeline reload while reading does not take it away.
 */
@HiltViewModel(assistedFactory = FeedItemReaderViewModel.Factory::class)
class FeedItemReaderViewModel
@AssistedInject
constructor(@Assisted route: FeedItemReader, loadedItems: LoadedFeedItems) : ViewModel() {

  /** The item to read, or null when the timeline no longer holds it */
  val item: FeedItem? = loadedItems.find(route.feedUrl, route.itemId)

  /** Creates the ViewModel for one route */
  @AssistedFactory
  interface Factory {

    /** Create the ViewModel for [route] */
    fun create(route: FeedItemReader): FeedItemReaderViewModel
  }
}
