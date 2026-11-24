package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.FeedTimeline
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.model.feedItem
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedTimelineUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

  private lateinit var loadFeedTimeline: LoadFeedTimelineUseCase
  private lateinit var loadedItems: LoadedFeedItems

  private val testDispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    loadFeedTimeline = mockk()
    loadedItems = LoadedFeedItems()
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel() = FeedViewModel(loadFeedTimeline, loadedItems)

  @Test
  fun `shows loading until the first load completes`() = runTest {
    val pending = CompletableDeferred<Result<FeedTimeline>>()
    coEvery { loadFeedTimeline() } coAnswers { pending.await() }

    val viewModel = createViewModel()
    runCurrent()

    assertEquals(FeedUiState.Content.Loading, viewModel.uiState.value.content)
    assertFalse(viewModel.uiState.value.isRefreshing, "the first load is not a refresh")
  }

  @Test
  fun `loads the timeline once when opened`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(FeedTimeline(listOf(ITEM), emptyList()))

    val viewModel = createViewModel()
    advanceUntilIdle()

    coVerify(exactly = 1) { loadFeedTimeline() }
    assertEquals(
        FeedUiState.Content.Loaded(listOf(ITEM), emptyList()), viewModel.uiState.value.content)
  }

  @Test
  fun `refresh reloads the timeline and reports refreshing while it runs`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(FeedTimeline(listOf(ITEM), emptyList()))
    val viewModel = createViewModel()
    advanceUntilIdle()
    val pending = CompletableDeferred<Result<FeedTimeline>>()
    coEvery { loadFeedTimeline() } coAnswers { pending.await() }

    viewModel.refresh()
    runCurrent()

    assertTrue(viewModel.uiState.value.isRefreshing, "refreshing while the reload runs")
    assertEquals(
        FeedUiState.Content.Loaded(listOf(ITEM), emptyList()),
        viewModel.uiState.value.content,
        "the previous items stay visible while refreshing")

    pending.complete(Result.success(FeedTimeline(listOf(OTHER_ITEM), emptyList())))
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isRefreshing, "refreshing ends with the reload")
    assertEquals(
        FeedUiState.Content.Loaded(listOf(OTHER_ITEM), emptyList()),
        viewModel.uiState.value.content)
    coVerify(exactly = 2) { loadFeedTimeline() }
  }

  @Test
  fun `refresh is ignored while a load is still running`() = runTest {
    val pending = CompletableDeferred<Result<FeedTimeline>>()
    coEvery { loadFeedTimeline() } coAnswers { pending.await() }
    val viewModel = createViewModel()
    runCurrent()

    viewModel.refresh()
    runCurrent()

    coVerify(exactly = 1) { loadFeedTimeline() }
    assertFalse(viewModel.uiState.value.isRefreshing, "an ignored refresh does not show progress")
  }

  @Test
  fun `reloading after the subscriptions changed restarts a load that is still running`() =
      runTest {
        val stale = CompletableDeferred<Result<FeedTimeline>>()
        coEvery { loadFeedTimeline() } coAnswers { stale.await() }
        val viewModel = createViewModel()
        runCurrent()
        coEvery { loadFeedTimeline() } returns
            Result.success(FeedTimeline(listOf(OTHER_ITEM), emptyList()))

        viewModel.reloadAfterSubscriptionChange()
        advanceUntilIdle()
        stale.complete(Result.success(FeedTimeline(listOf(ITEM), emptyList())))
        advanceUntilIdle()

        coVerify(exactly = 2) { loadFeedTimeline() }
        assertEquals(
            FeedUiState.Content.Loaded(listOf(OTHER_ITEM), emptyList()),
            viewModel.uiState.value.content,
            "the load started before the change must not overwrite the new timeline")
        assertFalse(viewModel.uiState.value.isRefreshing)
      }

  @Test
  fun `a timeline without items or failures is empty`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(FeedTimeline(emptyList(), emptyList()))

    val viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(FeedUiState.Content.Empty, viewModel.uiState.value.content)
  }

  @Test
  fun `an unavailable subscription list is an error with its reason, not empty`() = runTest {
    val expected =
        mapOf(
            FeedSubscriptionLoadResult.Reason.NO_USER_RELAYS to FeedLoadError.NoUserRelays,
            FeedSubscriptionLoadResult.Reason.ACCESS_DENIED to FeedLoadError.AccessDenied,
            FeedSubscriptionLoadResult.Reason.UNREACHABLE to FeedLoadError.Unreachable,
        )
    FeedSubscriptionLoadResult.Reason.entries.forEach { reason ->
      coEvery { loadFeedTimeline() } returns
          Result.failure(FeedSubscriptionListUnavailableException(reason))

      val viewModel = createViewModel()
      advanceUntilIdle()

      assertEquals(
          FeedUiState.Content.Error(expected.getValue(reason)),
          viewModel.uiState.value.content,
          "reason $reason")
    }
  }

  @Test
  fun `feeds that failed are listed next to the items that loaded`() = runTest {
    val failure = FeedFetchFailure("https://broken.example/feed", IOException("timeout"))
    coEvery { loadFeedTimeline() } returns
        Result.success(FeedTimeline(listOf(ITEM), listOf(failure)))

    val viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(
        FeedUiState.Content.Loaded(listOf(ITEM), listOf(failure)), viewModel.uiState.value.content)
  }

  @Test
  fun `a timeline whose every feed failed is loaded with the failures, not empty`() = runTest {
    val failure = FeedFetchFailure("https://broken.example/feed", IOException("timeout"))
    coEvery { loadFeedTimeline() } returns
        Result.success(FeedTimeline(emptyList(), listOf(failure)))

    val viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(
        FeedUiState.Content.Loaded(emptyList(), listOf(failure)), viewModel.uiState.value.content)
  }

  @Test
  fun `an item repeated within its feed is shown once`() = runTest {
    val repeated = ITEM.copy(title = "First, again")
    val sameIdOtherFeed = ITEM.copy(feedUrl = "https://b.example/feed")
    coEvery { loadFeedTimeline() } returns
        Result.success(FeedTimeline(listOf(ITEM, repeated, sameIdOtherFeed), emptyList()))

    val viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(
        FeedUiState.Content.Loaded(listOf(ITEM, sameIdOtherFeed), emptyList()),
        viewModel.uiState.value.content)
  }

  @Test
  fun `the items shown are shared so the reader can open them`() = runTest {
    val repeated = ITEM.copy(title = "First, again")
    coEvery { loadFeedTimeline() } returns
        Result.success(FeedTimeline(listOf(ITEM, repeated, OTHER_ITEM), emptyList()))

    createViewModel()
    advanceUntilIdle()

    assertEquals(ITEM, loadedItems.find(ITEM.feedUrl, ITEM.id), "the card shown is the one opened")
    assertEquals(OTHER_ITEM, loadedItems.find(OTHER_ITEM.feedUrl, OTHER_ITEM.id))
  }

  @Test
  fun `a refresh shares the reloaded items in place of the previous ones`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(FeedTimeline(listOf(ITEM), emptyList()))
    val viewModel = createViewModel()
    advanceUntilIdle()
    coEvery { loadFeedTimeline() } returns
        Result.success(FeedTimeline(listOf(OTHER_ITEM), emptyList()))

    viewModel.refresh()
    advanceUntilIdle()

    assertNull(loadedItems.find(ITEM.feedUrl, ITEM.id))
    assertEquals(OTHER_ITEM, loadedItems.find(OTHER_ITEM.feedUrl, OTHER_ITEM.id))
  }

  @Test
  fun `any other failure is an unknown error`() = runTest {
    coEvery { loadFeedTimeline() } returns
        Result.failure(IllegalStateException("User not logged in"))

    val viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(FeedUiState.Content.Error(FeedLoadError.Unknown), viewModel.uiState.value.content)
  }

  @Test
  fun `a failed refresh replaces the items with the error`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(FeedTimeline(listOf(ITEM), emptyList()))
    val viewModel = createViewModel()
    advanceUntilIdle()
    coEvery { loadFeedTimeline() } returns
        Result.failure(
            FeedSubscriptionListUnavailableException(FeedSubscriptionLoadResult.Reason.UNREACHABLE))

    viewModel.refresh()
    advanceUntilIdle()

    assertEquals(
        FeedUiState.Content.Error(FeedLoadError.Unreachable), viewModel.uiState.value.content)
    assertFalse(viewModel.uiState.value.isRefreshing)
  }

  @Test
  fun `the tabs name every category in name order ignoring case`() = runTest {
    coEvery { loadFeedTimeline() } returns
        Result.success(
            FeedTimeline(
                listOf(ITEM), emptyList(), categories = listOf("tech", "News", "Tech", "art")))

    val viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(listOf("art", "News", "tech", "Tech"), viewModel.uiState.value.categories)
  }

  @Test
  fun `a timeline opens on All with every item`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(CATEGORIZED)

    val viewModel = createViewModel()
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.selectedCategory)
    assertEquals(
        FeedUiState.Content.Loaded(CATEGORIZED.items, CATEGORIZED.failedFeeds),
        viewModel.uiState.value.content)
  }

  @Test
  fun `selecting a category shows only its items and failures without loading again`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(CATEGORIZED)
    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectCategory("Tech")
    advanceUntilIdle()

    assertEquals("Tech", viewModel.uiState.value.selectedCategory)
    assertEquals(
        FeedUiState.Content.Loaded(listOf(TECH_ITEM), listOf(TECH_FAILURE)),
        viewModel.uiState.value.contentIn("Tech"))
    coVerify(exactly = 1) { loadFeedTimeline() }
  }

  @Test
  fun `selecting a category keeps the whole timeline for the pages of the other tabs`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(CATEGORIZED)
    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectCategory("Tech")

    val state = viewModel.uiState.value
    assertEquals(
        FeedUiState.Content.Loaded(CATEGORIZED.items, CATEGORIZED.failedFeeds), state.content)
    assertEquals(
        FeedUiState.Content.Loaded(listOf(NEWS_ITEM), emptyList()), state.contentIn("News"))
  }

  @Test
  fun `uncategorized items are shown only under All`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(CATEGORIZED)
    val viewModel = createViewModel()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    val shownPerTab =
        (listOf(null) + state.categories).associateWith { category ->
          (state.contentIn(category) as FeedUiState.Content.Loaded).items
        }

    assertEquals(
        mapOf(
            null to CATEGORIZED.items,
            "News" to listOf(NEWS_ITEM),
            "Tech" to listOf(TECH_ITEM),
        ),
        shownPerTab)
  }

  @Test
  fun `a category whose feeds have no items shows an empty list instead of the empty state`() =
      runTest {
        coEvery { loadFeedTimeline() } returns
            Result.success(FeedTimeline(listOf(ITEM), emptyList(), categories = listOf("Quiet")))
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            FeedUiState.Content.Loaded(emptyList(), emptyList()),
            viewModel.uiState.value.contentIn("Quiet"))
      }

  @Test
  fun `the selected category stays selected across a refresh while it still exists`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(CATEGORIZED)
    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.selectCategory("News")
    val newer = NEWS_ITEM.copy(id = "n2", title = "Newer news")
    coEvery { loadFeedTimeline() } returns
        Result.success(CATEGORIZED.copy(items = listOf(newer) + CATEGORIZED.items))

    viewModel.refresh()
    advanceUntilIdle()

    assertEquals("News", viewModel.uiState.value.selectedCategory)
    assertEquals(
        FeedUiState.Content.Loaded(listOf(newer, NEWS_ITEM), emptyList()),
        viewModel.uiState.value.contentIn("News"))
  }

  @Test
  fun `the selected category falls back to All when a reload no longer has it`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(CATEGORIZED)
    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.selectCategory("News")
    val withoutNews =
        FeedTimeline(listOf(TECH_ITEM, ITEM), listOf(TECH_FAILURE), categories = listOf("Tech"))
    coEvery { loadFeedTimeline() } returns Result.success(withoutNews)

    viewModel.reloadAfterSubscriptionChange()
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.selectedCategory)
    assertEquals(listOf("Tech"), viewModel.uiState.value.categories)
    assertEquals(
        FeedUiState.Content.Loaded(withoutNews.items, withoutNews.failedFeeds),
        viewModel.uiState.value.content)
  }

  @Test
  fun `the reader can open items of every category whatever tab is selected`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(CATEGORIZED)
    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectCategory("Tech")

    assertEquals(NEWS_ITEM, loadedItems.find(NEWS_ITEM.feedUrl, NEWS_ITEM.id))
    assertEquals(ITEM, loadedItems.find(ITEM.feedUrl, ITEM.id))
  }

  @Test
  fun `a failed load drops the tabs and returns to All`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(CATEGORIZED)
    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.selectCategory("Tech")
    coEvery { loadFeedTimeline() } returns Result.failure(IOException("offline"))

    viewModel.refresh()
    advanceUntilIdle()

    assertEquals(emptyList(), viewModel.uiState.value.categories)
    assertNull(viewModel.uiState.value.selectedCategory)
    assertEquals(FeedUiState.Content.Error(FeedLoadError.Unknown), viewModel.uiState.value.content)
  }

  private companion object {
    val ITEM = feedItem(publishedAt = Instant.parse("2026-01-01T00:00:00Z"))
    val OTHER_ITEM = ITEM.copy(id = "2", title = "Second", link = "https://a.example/2")
    val TECH_ITEM =
        ITEM.copy(
            id = "t", feedUrl = "https://tech.example/feed", title = "Tech", feedCategory = "Tech")
    val NEWS_ITEM =
        ITEM.copy(
            id = "n", feedUrl = "https://news.example/feed", title = "News", feedCategory = "News")
    val TECH_FAILURE =
        FeedFetchFailure("https://broken-tech.example/feed", IOException("timeout"), "Tech")
    val CATEGORIZED =
        FeedTimeline(
            items = listOf(TECH_ITEM, NEWS_ITEM, ITEM),
            failedFeeds = listOf(TECH_FAILURE),
            categories = listOf("Tech", "News"))
  }
}
