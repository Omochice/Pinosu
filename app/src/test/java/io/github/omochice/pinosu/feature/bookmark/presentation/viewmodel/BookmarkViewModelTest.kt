package io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkList
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.feature.bookmark.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Unit tests for [BookmarkViewModel]
 *
 * Tests cover:
 * - Per-tab lazy loading and refresh (Local queries the current user, Global queries all authors)
 * - Per-tab pagination with an inclusive cursor and hasMore-driven termination
 * - Multiple URLs dialog and error dialog state management
 * - Display mode observation
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkViewModelTest {

  private lateinit var getBookmarkListUseCase: GetBookmarkListUseCase
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var observeDisplayModeUseCase: ObserveDisplayModeUseCase
  private lateinit var displayModeFlow: MutableStateFlow<BookmarkDisplayMode>
  private lateinit var viewModel: BookmarkViewModel

  private val testDispatcher = StandardTestDispatcher()

  private val testNpub = "npub1" + "a".repeat(58)

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    getBookmarkListUseCase = mockk(relaxed = true)
    getLoginStateUseCase = mockk(relaxed = true)
    displayModeFlow = MutableStateFlow(BookmarkDisplayMode.List)
    observeDisplayModeUseCase = mockk()
    every { observeDisplayModeUseCase() } returns displayModeFlow
    viewModel =
        BookmarkViewModel(getBookmarkListUseCase, getLoginStateUseCase, observeDisplayModeUseCase)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun bookmark(id: String, createdAt: Long): BookmarkItem =
      BookmarkItem(
          type = "event",
          eventId = id,
          title = "Bookmark $id",
          event =
              BookmarkedEvent(
                  kind = 39_701, content = "", author = "author", createdAt = createdAt))

  @Test
  fun `initial BookmarkUiState should have default values`() = runTest {
    val state = viewModel.uiState.first()

    assertFalse(state.local.isLoading, "local isLoading should be false")
    assertFalse(state.global.isLoading, "global isLoading should be false")
    assertNull(state.local.error, "local error should be null")
    assertNull(state.selectedBookmarkForUrlDialog, "selectedBookmarkForUrlDialog should be null")
    assertNull(state.urlOpenError, "urlOpenError should be null")
  }

  @Test
  fun `selectTab to Global should update selectedTab`() = runTest {
    viewModel.selectTab(BookmarkFilterMode.Global)

    val state = viewModel.uiState.first()
    assertEquals(
        BookmarkFilterMode.Global,
        state.selectedTab,
        "selectedTab should be Global after selecting")
  }

  @Test
  fun `selectTab with same tab should not trigger state update`() = runTest {
    val initialState = viewModel.uiState.first()
    viewModel.selectTab(BookmarkFilterMode.Local)
    val newState = viewModel.uiState.first()

    assertSame(initialState, newState, "State should be same reference when selecting same tab")
  }

  @Test
  fun `onBookmarkCardClicked with multiple URLs should set selectedBookmarkForUrlDialog`() =
      runTest {
        val bookmarkWithMultipleUrls =
            BookmarkItem(
                type = "event",
                eventId = "test789",
                title = "Multiple URLs Bookmark",
                urls =
                    listOf(
                        "https://example.com/1", "https://example.com/2", "https://example.com/3"))

        viewModel.onBookmarkCardClicked(bookmarkWithMultipleUrls)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(
            state.selectedBookmarkForUrlDialog,
            "selectedBookmarkForUrlDialog should be set for multiple URLs")
        assertEquals(
            bookmarkWithMultipleUrls,
            state.selectedBookmarkForUrlDialog,
            "selectedBookmarkForUrlDialog should be the clicked bookmark")
      }

  @Test
  fun `dismissUrlDialog should clear selectedBookmarkForUrlDialog`() = runTest {
    val bookmarkWithMultipleUrls =
        BookmarkItem(
            type = "event",
            eventId = "test999",
            title = "Test Dismiss",
            urls = listOf("https://example.com/a", "https://example.com/b"))

    viewModel.onBookmarkCardClicked(bookmarkWithMultipleUrls)
    advanceUntilIdle()

    viewModel.dismissUrlDialog()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNull(
        state.selectedBookmarkForUrlDialog,
        "selectedBookmarkForUrlDialog should be null after dismissing")
  }

  @Test
  fun `setUrlOpenError should set error message`() = runTest {
    val errorMessage = "Failed to open URL"

    viewModel.setUrlOpenError(errorMessage)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.urlOpenError, "urlOpenError should be set")
    assertEquals(errorMessage, state.urlOpenError, "urlOpenError should contain the error message")
  }

  @Test
  fun `dismissErrorDialog should clear urlOpenError`() = runTest {
    viewModel.setUrlOpenError("Some error")
    advanceUntilIdle()

    viewModel.dismissErrorDialog()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNull(state.urlOpenError, "urlOpenError should be null after dismissing")
  }

  @Test
  fun `loadTab Local should set isLoading to true initially`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any()) } coAnswers
        {
          kotlinx.coroutines.delay(100)
          Result.success(BookmarkList("test", emptyList(), 0L))
        }

    viewModel.loadTab(BookmarkFilterMode.Local)
    testScheduler.runCurrent()

    val state = viewModel.uiState.first()
    assertTrue(state.local.isLoading, "local isLoading should be true during loading")
  }

  @Test
  fun `loadTab Local should store bookmarks in the local tab`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    val testBookmarks = listOf(bookmark("id1", 1_700_000_000L))
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any()) } returns
        Result.success(BookmarkList("test", testBookmarks, 0L))

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.local.isLoading, "local isLoading should be false after loading")
    assertEquals(testBookmarks, state.local.items, "local items should be set")
    assertNull(state.local.error, "local error should be null on success")
  }

  @Test
  fun `loadTab Local should query with the user npub as author`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    val authorSlot = slot<String?>()
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(captureNullable(authorSlot), any()) } returns
        Result.success(BookmarkList("test", emptyList(), 0L))

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    assertEquals(testNpub, authorSlot.captured, "Local tab should query constrained to the user")
  }

  @Test
  fun `loadTab Global should query with a null author`() = runTest {
    val authorSlot = slot<String?>()
    coEvery { getBookmarkListUseCase(captureNullable(authorSlot), any()) } returns
        Result.success(BookmarkList("test", emptyList(), 0L))

    viewModel.loadTab(BookmarkFilterMode.Global)
    advanceUntilIdle()

    assertNull(authorSlot.captured, "Global tab should query without an author constraint")
  }

  @Test
  fun `loadTab Local should set error when not logged in`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.local.isLoading, "local isLoading should be false")
    assertEquals("Not logged in", state.local.error, "local error should indicate not logged in")
  }

  @Test
  fun `loadTab Local should set error on failure`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    val errorMessage = "Network error"
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any()) } returns Result.failure(Exception(errorMessage))

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.local.isLoading, "local isLoading should be false")
    assertEquals(errorMessage, state.local.error, "local error should be set")
  }

  @Test
  fun `loadTab is idempotent and does not refetch an already loaded tab`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any()) } returns
        Result.success(BookmarkList("test", emptyList(), 0L))

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()
    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    coVerify(exactly = 1) { getBookmarkListUseCase(any(), any()) }
  }

  @Test
  fun `loadMore appends new items to the local tab`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    val initialBookmarks = (1..10).map { i -> bookmark("id$i", 1_700_000_000L - i * 100) }
    val olderBookmarks = (11..15).map { i -> bookmark("id$i", 1_700_000_000L - i * 100) }

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L, hasMore = true))
          } else {
            Result.success(BookmarkList("test", olderBookmarks, 0L, hasMore = false))
          }
        }

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    viewModel.loadMore(BookmarkFilterMode.Local)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(
        15, state.local.items.size, "local items should contain both initial and older items")
    assertFalse(state.local.isLoadingMore, "isLoadingMore should be false after loading")
  }

  @Test
  fun `loadMore uses an inclusive until cursor equal to the oldest createdAt`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    val oldestCreatedAt = 1_700_000_000L - 10 * 100
    val initialBookmarks = (1..10).map { i -> bookmark("id$i", 1_700_000_000L - i * 100) }
    val untilSlot = slot<Long?>()

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), captureNullable(untilSlot)) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L, hasMore = true))
          } else {
            Result.success(BookmarkList("test", emptyList(), 0L, hasMore = false))
          }
        }

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    viewModel.loadMore(BookmarkFilterMode.Local)
    advanceUntilIdle()

    assertEquals(
        oldestCreatedAt,
        untilSlot.captured,
        "until should equal the oldest createdAt (inclusive per NIP-01)")
  }

  @Test
  fun `loadMore does nothing when isLoadingMore is true`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    val initialBookmarks = (1..10).map { i -> bookmark("id$i", 1_700_000_000L - i * 100) }

    var loadMoreCallCount = 0
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L, hasMore = true))
          } else {
            loadMoreCallCount++
            kotlinx.coroutines.delay(1000)
            Result.success(BookmarkList("test", emptyList(), 0L, hasMore = false))
          }
        }

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    viewModel.loadMore(BookmarkFilterMode.Local)
    testScheduler.runCurrent()

    viewModel.loadMore(BookmarkFilterMode.Local)
    advanceUntilIdle()

    assertEquals(1, loadMoreCallCount, "loadMore should only trigger one usecase call")
  }

  @Test
  fun `loadMore does nothing when hasMoreItems is false`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    val initialBookmarks = (1..10).map { i -> bookmark("id$i", 1_700_000_000L - i * 100) }

    var loadMoreCallCount = 0
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L, hasMore = true))
          } else {
            loadMoreCallCount++
            Result.success(BookmarkList("test", emptyList(), 0L, hasMore = false))
          }
        }

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    viewModel.loadMore(BookmarkFilterMode.Local)
    advanceUntilIdle()

    viewModel.loadMore(BookmarkFilterMode.Local)
    advanceUntilIdle()

    assertEquals(
        1,
        loadMoreCallCount,
        "loadMore should only be called once because hasMoreItems becomes false")
  }

  @Test
  fun `loadMore sets hasMoreItems to false when relay reports no more items`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    val initialBookmarks = (1..10).map { i -> bookmark("id$i", 1_700_000_000L - i * 100) }
    val fewOlderBookmarks = listOf(bookmark("id11", 1_699_998_000L))

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L, hasMore = true))
          } else {
            Result.success(BookmarkList("test", fewOlderBookmarks, 0L, hasMore = false))
          }
        }

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    viewModel.loadMore(BookmarkFilterMode.Local)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.local.hasMoreItems, "hasMoreItems should be false when relay has no more")
  }

  @Test
  fun `refresh resets hasMoreItems to true`() = runTest {
    val testUser = User(Pubkey.parse(testNpub)!!)
    val initialBookmarks = (1..10).map { i -> bookmark("id$i", 1_700_000_000L - i * 100) }

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L, hasMore = true))
          } else {
            Result.success(BookmarkList("test", emptyList(), 0L, hasMore = false))
          }
        }

    viewModel.loadTab(BookmarkFilterMode.Local)
    advanceUntilIdle()

    viewModel.loadMore(BookmarkFilterMode.Local)
    advanceUntilIdle()
    assertFalse(
        viewModel.uiState.first().local.hasMoreItems,
        "hasMoreItems should be false after exhausting")

    viewModel.loadTab(BookmarkFilterMode.Local, forceReload = true)
    advanceUntilIdle()

    assertTrue(
        viewModel.uiState.first().local.hasMoreItems, "hasMoreItems should be true after refresh")
  }

  @Test
  fun `observeDisplayMode should update uiState when display mode changes`() = runTest {
    assertEquals(
        BookmarkDisplayMode.List,
        viewModel.uiState.first().displayMode,
        "Initial display mode should be List")

    displayModeFlow.value = BookmarkDisplayMode.Grid
    advanceUntilIdle()

    assertEquals(
        BookmarkDisplayMode.Grid,
        viewModel.uiState.first().displayMode,
        "Display mode should be updated to Grid")
  }

  @Test
  fun `state should handle concurrent dialog states independently`() = runTest {
    val multiUrlBookmark =
        BookmarkItem(
            type = "event",
            eventId = "concurrent-test",
            title = "Concurrent Test",
            urls = listOf("https://example.com/1", "https://example.com/2"))

    viewModel.onBookmarkCardClicked(multiUrlBookmark)
    viewModel.setUrlOpenError("Test error")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.selectedBookmarkForUrlDialog, "URL dialog should be shown")
    assertNotNull(state.urlOpenError, "Error dialog should be shown")

    viewModel.dismissUrlDialog()
    advanceUntilIdle()

    val stateAfterDismissUrl = viewModel.uiState.first()
    assertNull(stateAfterDismissUrl.selectedBookmarkForUrlDialog, "URL dialog should be dismissed")
    assertNotNull(stateAfterDismissUrl.urlOpenError, "Error dialog should still be shown")
  }
}
