package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.FeedTimeline
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
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

  private val testDispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    loadFeedTimeline = mockk()
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `shows loading until the first load completes`() = runTest {
    val pending = CompletableDeferred<Result<FeedTimeline>>()
    coEvery { loadFeedTimeline() } coAnswers { pending.await() }

    val viewModel = FeedViewModel(loadFeedTimeline)
    runCurrent()

    assertEquals(FeedUiState.Content.Loading, viewModel.uiState.value.content)
    assertFalse(viewModel.uiState.value.isRefreshing, "the first load is not a refresh")
  }

  @Test
  fun `loads the timeline once when opened`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(FeedTimeline(listOf(ITEM), emptyList()))

    val viewModel = FeedViewModel(loadFeedTimeline)
    advanceUntilIdle()

    coVerify(exactly = 1) { loadFeedTimeline() }
    assertEquals(
        FeedUiState.Content.Loaded(listOf(ITEM), emptyList()), viewModel.uiState.value.content)
  }

  @Test
  fun `refresh reloads the timeline and reports refreshing while it runs`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(FeedTimeline(listOf(ITEM), emptyList()))
    val viewModel = FeedViewModel(loadFeedTimeline)
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
    val viewModel = FeedViewModel(loadFeedTimeline)
    runCurrent()

    viewModel.refresh()
    runCurrent()

    coVerify(exactly = 1) { loadFeedTimeline() }
    assertFalse(viewModel.uiState.value.isRefreshing, "an ignored refresh does not show progress")
  }

  @Test
  fun `a timeline without items or failures is empty`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(FeedTimeline(emptyList(), emptyList()))

    val viewModel = FeedViewModel(loadFeedTimeline)
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

      val viewModel = FeedViewModel(loadFeedTimeline)
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

    val viewModel = FeedViewModel(loadFeedTimeline)
    advanceUntilIdle()

    assertEquals(
        FeedUiState.Content.Loaded(listOf(ITEM), listOf(failure)), viewModel.uiState.value.content)
  }

  @Test
  fun `a timeline whose every feed failed is loaded with the failures, not empty`() = runTest {
    val failure = FeedFetchFailure("https://broken.example/feed", IOException("timeout"))
    coEvery { loadFeedTimeline() } returns
        Result.success(FeedTimeline(emptyList(), listOf(failure)))

    val viewModel = FeedViewModel(loadFeedTimeline)
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

    val viewModel = FeedViewModel(loadFeedTimeline)
    advanceUntilIdle()

    assertEquals(
        FeedUiState.Content.Loaded(listOf(ITEM, sameIdOtherFeed), emptyList()),
        viewModel.uiState.value.content)
  }

  @Test
  fun `any other failure is an unknown error`() = runTest {
    coEvery { loadFeedTimeline() } returns
        Result.failure(IllegalStateException("User not logged in"))

    val viewModel = FeedViewModel(loadFeedTimeline)
    advanceUntilIdle()

    assertEquals(FeedUiState.Content.Error(FeedLoadError.Unknown), viewModel.uiState.value.content)
  }

  @Test
  fun `a failed refresh replaces the items with the error`() = runTest {
    coEvery { loadFeedTimeline() } returns Result.success(FeedTimeline(listOf(ITEM), emptyList()))
    val viewModel = FeedViewModel(loadFeedTimeline)
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

  private companion object {
    val ITEM =
        FeedItem(
            id = "1",
            feedUrl = "https://a.example/feed",
            feedTitle = "A",
            title = "First",
            link = "https://a.example/1",
            publishedAt = Instant.parse("2026-01-01T00:00:00Z"),
            summary = "Summary")
    val OTHER_ITEM = ITEM.copy(id = "2", title = "Second", link = "https://a.example/2")
  }
}
