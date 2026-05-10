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
 * Unit tests for BookmarkViewModel URL click functionality
 *
 * Tests cover:
 * - Multiple URLs dialog state management
 * - Error dialog state management
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

  @Test
  fun `initial BookmarkUiState should have default values`() = runTest {
    val state = viewModel.uiState.first()

    assertFalse(state.isLoading, "isLoading should be false")
    assertNull(state.error, "error should be null")
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
  fun `loadBookmarks should set isLoading to true initially`() = runTest {
    val testUser = User(Pubkey.parse("npub1" + "a".repeat(58))!!)
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any()) } coAnswers
        {
          kotlinx.coroutines.delay(100)
          Result.success(BookmarkList("test", emptyList(), 0L))
        }

    viewModel.loadBookmarks()
    testScheduler.runCurrent()

    val state = viewModel.uiState.first()
    assertTrue(state.isLoading, "isLoading should be true during loading")
  }

  @Test
  fun `loadBookmarks should store bookmarks in allBookmarks`() = runTest {
    val testUser = User(Pubkey.parse("npub1" + "a".repeat(58))!!)
    val testBookmarks =
        listOf(
            BookmarkItem(
                type = "event",
                eventId = "id1",
                title = "Bookmark 1",
                urls = listOf("https://example.com/1")))
    val bookmarkList = BookmarkList("test", testBookmarks, 0L)

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any()) } returns Result.success(bookmarkList)

    viewModel.loadBookmarks()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.isLoading, "isLoading should be false after loading")
    assertEquals(testBookmarks, state.allBookmarks, "allBookmarks should be set")
    assertNull(state.error, "error should be null on success")
  }

  @Test
  fun `loadBookmarks should set error when not logged in`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    viewModel.loadBookmarks()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.isLoading, "isLoading should be false")
    assertEquals("Not logged in", state.error, "error should indicate not logged in")
  }

  @Test
  fun `loadBookmarks should set error on failure`() = runTest {
    val testUser = User(Pubkey.parse("npub1" + "a".repeat(58))!!)
    val errorMessage = "Network error"
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any()) } returns Result.failure(Exception(errorMessage))

    viewModel.loadBookmarks()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.isLoading, "isLoading should be false")
    assertEquals(errorMessage, state.error, "error should be set")
  }

  @Test
  fun `loadBookmarks should fetch bookmarks from relays`() = runTest {
    val testUser = User(Pubkey.parse("npub1" + "a".repeat(58))!!)
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any()) } returns
        Result.success(BookmarkList("test", emptyList(), 0L))

    viewModel.loadBookmarks()
    advanceUntilIdle()

    coVerify { getBookmarkListUseCase(any()) }
  }

  @Test
  fun `multiple URL dialog workflow should work correctly`() = runTest {
    val bookmark =
        BookmarkItem(
            type = "event",
            eventId = "workflow-test",
            title = "Workflow Test",
            urls = listOf("https://example.com/1", "https://example.com/2"))

    viewModel.onBookmarkCardClicked(bookmark)
    advanceUntilIdle()

    var state = viewModel.uiState.first()
    assertNotNull(state.selectedBookmarkForUrlDialog, "Dialog should be shown")

    viewModel.dismissUrlDialog()
    advanceUntilIdle()

    state = viewModel.uiState.first()
    assertNull(state.selectedBookmarkForUrlDialog, "Dialog should be dismissed")
  }

  @Test
  fun `error dialog workflow should work correctly`() = runTest {
    viewModel.setUrlOpenError("Test error")
    advanceUntilIdle()

    var state = viewModel.uiState.first()
    assertNotNull(state.urlOpenError, "Error should be shown")

    viewModel.dismissErrorDialog()
    advanceUntilIdle()

    state = viewModel.uiState.first()
    assertNull(state.urlOpenError, "Error should be dismissed")
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
    val bookmark =
        BookmarkItem(
            type = "event",
            eventId = "concurrent-test",
            title = "Concurrent Test",
            urls = listOf("https://example.com/1", "https://example.com/2"))

    viewModel.onBookmarkCardClicked(bookmark)
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

  @Test
  fun `loadMore appends new items to existing allBookmarks`() = runTest {
    val testUser = User(Pubkey.parse("npub1" + "a".repeat(58))!!)
    val initialBookmarks =
        (1..10).map { i ->
          BookmarkItem(
              type = "event",
              eventId = "id$i",
              title = "Bookmark $i",
              event =
                  BookmarkedEvent(
                      kind = 39_701,
                      content = "",
                      author = "author",
                      createdAt = 1_700_000_000L - i * 100))
        }
    val olderBookmarks =
        (11..15).map { i ->
          BookmarkItem(
              type = "event",
              eventId = "id$i",
              title = "Bookmark $i",
              event =
                  BookmarkedEvent(
                      kind = 39_701,
                      content = "",
                      author = "author",
                      createdAt = 1_700_000_000L - i * 100))
        }

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any<Long>()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L))
          } else {
            Result.success(BookmarkList("test", olderBookmarks, 0L))
          }
        }

    viewModel.loadBookmarks()
    advanceUntilIdle()

    viewModel.loadMore()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(
        15, state.allBookmarks.size, "allBookmarks should contain both initial and older items")
    assertFalse(state.isLoadingMore, "isLoadingMore should be false after loading")
  }

  @Test
  fun `loadMore does nothing when isLoadingMore is true`() = runTest {
    val testUser = User(Pubkey.parse("npub1" + "a".repeat(58))!!)
    val initialBookmarks =
        (1..10).map { i ->
          BookmarkItem(
              type = "event",
              eventId = "id$i",
              title = "Bookmark $i",
              event =
                  BookmarkedEvent(
                      kind = 39_701,
                      content = "",
                      author = "author",
                      createdAt = 1_700_000_000L - i * 100))
        }

    var loadMoreCallCount = 0
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any<Long>()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L))
          } else {
            loadMoreCallCount++
            kotlinx.coroutines.delay(1000)
            Result.success(BookmarkList("test", emptyList(), 0L))
          }
        }

    viewModel.loadBookmarks()
    advanceUntilIdle()

    viewModel.loadMore()
    testScheduler.runCurrent()

    viewModel.loadMore()
    advanceUntilIdle()

    assertEquals(1, loadMoreCallCount, "loadMore should only trigger one usecase call")
  }

  @Test
  fun `loadMore does nothing when hasMoreItems is false`() = runTest {
    val testUser = User(Pubkey.parse("npub1" + "a".repeat(58))!!)
    val initialBookmarks =
        (1..10).map { i ->
          BookmarkItem(
              type = "event",
              eventId = "id$i",
              title = "Bookmark $i",
              event =
                  BookmarkedEvent(
                      kind = 39_701,
                      content = "",
                      author = "author",
                      createdAt = 1_700_000_000L - i * 100))
        }

    var loadMoreCallCount = 0
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any<Long>()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L))
          } else {
            loadMoreCallCount++
            Result.success(BookmarkList("test", emptyList(), 0L))
          }
        }

    viewModel.loadBookmarks()
    advanceUntilIdle()

    viewModel.loadMore()
    advanceUntilIdle()

    viewModel.loadMore()
    advanceUntilIdle()

    assertEquals(
        1,
        loadMoreCallCount,
        "loadMore should only be called once because hasMoreItems becomes false")
  }

  @Test
  fun `loadMore sets hasMoreItems to false when fewer items than page size returned`() = runTest {
    val testUser = User(Pubkey.parse("npub1" + "a".repeat(58))!!)
    val initialBookmarks =
        (1..10).map { i ->
          BookmarkItem(
              type = "event",
              eventId = "id$i",
              title = "Bookmark $i",
              event =
                  BookmarkedEvent(
                      kind = 39_701,
                      content = "",
                      author = "author",
                      createdAt = 1_700_000_000L - i * 100))
        }
    val fewOlderBookmarks =
        listOf(
            BookmarkItem(
                type = "event",
                eventId = "id11",
                title = "Bookmark 11",
                event =
                    BookmarkedEvent(
                        kind = 39_701,
                        content = "",
                        author = "author",
                        createdAt = 1_699_998_000L)))

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any<Long>()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L))
          } else {
            Result.success(BookmarkList("test", fewOlderBookmarks, 0L))
          }
        }

    viewModel.loadBookmarks()
    advanceUntilIdle()

    viewModel.loadMore()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.hasMoreItems, "hasMoreItems should be false when fewer than page size")
  }

  @Test
  fun `refresh resets hasMoreItems to true`() = runTest {
    val testUser = User(Pubkey.parse("npub1" + "a".repeat(58))!!)
    val initialBookmarks =
        (1..10).map { i ->
          BookmarkItem(
              type = "event",
              eventId = "id$i",
              title = "Bookmark $i",
              event =
                  BookmarkedEvent(
                      kind = 39_701,
                      content = "",
                      author = "author",
                      createdAt = 1_700_000_000L - i * 100))
        }

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any(), any<Long>()) } coAnswers
        {
          val until = secondArg<Long?>()
          if (until == null) {
            Result.success(BookmarkList("test", initialBookmarks, 0L))
          } else {
            Result.success(BookmarkList("test", emptyList(), 0L))
          }
        }

    viewModel.loadBookmarks()
    advanceUntilIdle()

    viewModel.loadMore()
    advanceUntilIdle()
    assertFalse(
        viewModel.uiState.first().hasMoreItems, "hasMoreItems should be false after exhausting")

    viewModel.loadBookmarks()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.first().hasMoreItems, "hasMoreItems should be true after refresh")
  }
}
