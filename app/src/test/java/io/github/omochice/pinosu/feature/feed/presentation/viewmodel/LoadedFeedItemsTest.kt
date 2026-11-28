package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.feature.feed.domain.model.feedItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoadedFeedItemsTest {

  @Test
  fun `nothing is found before any timeline has loaded`() {
    val loaded = LoadedFeedItems()

    assertNull(loaded.find(ITEM.feedUrl, ITEM.id))
  }

  @Test
  fun `an item of the last loaded timeline is found by its feed url and id`() {
    val loaded = LoadedFeedItems()

    loaded.replace(listOf(ITEM, OTHER_FEED_ITEM))

    assertEquals(ITEM, loaded.find(ITEM.feedUrl, ITEM.id))
    assertEquals(OTHER_FEED_ITEM, loaded.find(OTHER_FEED_ITEM.feedUrl, OTHER_FEED_ITEM.id))
  }

  @Test
  fun `the same id in another feed is not the same item`() {
    val loaded = LoadedFeedItems()

    loaded.replace(listOf(ITEM))

    assertNull(loaded.find("https://b.example/feed", ITEM.id))
  }

  @Test
  fun `a newer timeline replaces the items of the previous one`() {
    val loaded = LoadedFeedItems()
    loaded.replace(listOf(ITEM))

    loaded.replace(listOf(OTHER_FEED_ITEM))

    assertNull(loaded.find(ITEM.feedUrl, ITEM.id))
    assertEquals(OTHER_FEED_ITEM, loaded.find(OTHER_FEED_ITEM.feedUrl, OTHER_FEED_ITEM.id))
  }

  private companion object {
    val ITEM = feedItem(contentHtml = "<p>Body</p>")
    val OTHER_FEED_ITEM = ITEM.copy(feedUrl = "https://b.example/feed", title = "B first")
  }
}
