package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.core.navigation.FeedItemReader
import io.github.omochice.pinosu.feature.feed.domain.model.feedItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeedItemReaderViewModelTest {

  private val loadedItems = LoadedFeedItems().apply { replace(listOf(ITEM)) }

  @Test
  fun `reads the item the route names from the loaded timeline`() {
    val viewModel = FeedItemReaderViewModel(FeedItemReader(ITEM.feedUrl, ITEM.id), loadedItems)

    assertEquals(ITEM, viewModel.item)
  }

  @Test
  fun `has no item when the timeline no longer holds it, as after process death`() {
    val viewModel =
        FeedItemReaderViewModel(FeedItemReader(ITEM.feedUrl, ITEM.id), LoadedFeedItems())

    assertNull(viewModel.item)
  }

  @Test
  fun `has no item when the route names an item of another feed`() {
    val viewModel =
        FeedItemReaderViewModel(FeedItemReader("https://b.example/feed", ITEM.id), loadedItems)

    assertNull(viewModel.item)
  }

  @Test
  fun `keeps the item it opened with when the timeline reloads without it`() {
    val viewModel = FeedItemReaderViewModel(FeedItemReader(ITEM.feedUrl, ITEM.id), loadedItems)

    loadedItems.replace(emptyList())

    assertEquals(ITEM, viewModel.item)
  }

  private companion object {
    val ITEM = feedItem(id = "urn:1", contentHtml = "<p>Body</p>")
  }
}
