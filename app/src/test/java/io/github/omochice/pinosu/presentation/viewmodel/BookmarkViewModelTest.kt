package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.domain.model.BookmarkList
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for BookmarkViewModel URL click functionality
 *
 * Tests cover:
 * - Multiple URLs dialog state management
 * - Error dialog state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkViewModelTest {

  private lateinit var getBookmarkListUseCase: GetBookmarkListUseCase
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var viewModel: BookmarkViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    getBookmarkListUseCase = mockk(relaxed = true)
    getLoginStateUseCase = mockk(relaxed = true)
    viewModel = BookmarkViewModel(getBookmarkListUseCase, getLoginStateUseCase)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial BookmarkUiState should have default values`() = runTest {
    val state = viewModel.uiState.first()

    assertFalse("isLoading should be false", state.isLoading)
    assertTrue("bookmarks should be empty", state.bookmarks.isEmpty())
    assertNull("error should be null", state.error)
    assertNull("selectedBookmarkForUrlDialog should be null", state.selectedBookmarkForUrlDialog)
    assertNull("urlOpenError should be null", state.urlOpenError)
  }

  @Test
  fun `selectTab to Global should update selectedTab`() = runTest {
    viewModel.selectTab(BookmarkFilterMode.Global)

    val state = viewModel.uiState.first()
    assertEquals(
        "selectedTab should be Global after selecting",
        BookmarkFilterMode.Global,
        state.selectedTab)
  }

  @Test
  fun `selectTab with same tab should not trigger state update`() = runTest {
    val initialState = viewModel.uiState.first()
    viewModel.selectTab(BookmarkFilterMode.Local)
    val newState = viewModel.uiState.first()

    assertSame("State should be same reference when selecting same tab", initialState, newState)
  }

  @Test
  fun `Global tab should show all bookmarks from allBookmarks`() = runTest {
    val testBookmarks =
        listOf(
            createTestBookmarkItem("id1", "author1"),
            createTestBookmarkItem("id2", "author2"),
            createTestBookmarkItem("id3", "author1"))

    setViewModelState(allBookmarks = testBookmarks, userHexPubkey = "author1")

    viewModel.selectTab(BookmarkFilterMode.Global)

    val state = viewModel.uiState.first()
    assertEquals("Global tab should show all bookmarks", testBookmarks, state.bookmarks)
  }

  @Test
  fun `Local tab should filter bookmarks by userHexPubkey`() = runTest {
    val testBookmarks =
        listOf(
            createTestBookmarkItem("id1", "author1"),
            createTestBookmarkItem("id2", "author2"),
            createTestBookmarkItem("id3", "author1"))

    setViewModelState(
        allBookmarks = testBookmarks,
        userHexPubkey = "author1",
        selectedTab = BookmarkFilterMode.Global)

    viewModel.selectTab(BookmarkFilterMode.Local)

    val state = viewModel.uiState.first()
    assertEquals("Local tab should show only user's bookmarks", 2, state.bookmarks.size)
    assertTrue(
        "All bookmarks should have matching author",
        state.bookmarks.all { it.event?.author == "author1" })
  }

  @Test
  fun `Local tab with null userHexPubkey should show empty list`() = runTest {
    val testBookmarks =
        listOf(createTestBookmarkItem("id1", "author1"), createTestBookmarkItem("id2", "author2"))

    setViewModelState(allBookmarks = testBookmarks, userHexPubkey = null)

    viewModel.selectTab(BookmarkFilterMode.Local)

    val state = viewModel.uiState.first()
    assertTrue("Local tab with null userHexPubkey should be empty", state.bookmarks.isEmpty())
  }

  private fun createTestBookmarkItem(eventId: String, author: String): BookmarkItem {
    return BookmarkItem(
        type = "event",
        eventId = eventId,
        title = "Test $eventId",
        urls = listOf("https://example.com/$eventId"),
        event =
            io.github.omochice.pinosu.domain.model.BookmarkedEvent(
                kind = 39701,
                content = "content",
                author = author,
                createdAt = System.currentTimeMillis() / 1000,
                tags = emptyList()))
  }

  private fun setViewModelState(
      allBookmarks: List<BookmarkItem> = emptyList(),
      userHexPubkey: String? = null,
      selectedTab: BookmarkFilterMode = BookmarkFilterMode.Local
  ) {
    val currentState = viewModel.uiState.value
    val field = viewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val mutableStateFlow =
        field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<BookmarkUiState>
    mutableStateFlow.value =
        currentState.copy(
            allBookmarks = allBookmarks, userHexPubkey = userHexPubkey, selectedTab = selectedTab)
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
            "selectedBookmarkForUrlDialog should be set for multiple URLs",
            state.selectedBookmarkForUrlDialog)
        assertEquals(
            "selectedBookmarkForUrlDialog should be the clicked bookmark",
            bookmarkWithMultipleUrls,
            state.selectedBookmarkForUrlDialog)
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
        "selectedBookmarkForUrlDialog should be null after dismissing",
        state.selectedBookmarkForUrlDialog)
  }

  @Test
  fun `setUrlOpenError should set error message`() = runTest {
    val errorMessage = "Failed to open URL"

    viewModel.setUrlOpenError(errorMessage)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull("urlOpenError should be set", state.urlOpenError)
    assertEquals("urlOpenError should contain the error message", errorMessage, state.urlOpenError)
  }

  @Test
  fun `dismissErrorDialog should clear urlOpenError`() = runTest {
    viewModel.setUrlOpenError("Some error")
    advanceUntilIdle()

    viewModel.dismissErrorDialog()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNull("urlOpenError should be null after dismissing", state.urlOpenError)
  }

  @Test
  fun `loadBookmarks should set isLoading to true initially`() = runTest {
    val testUser = User("npub1test")
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any()) } coAnswers
        {
          kotlinx.coroutines.delay(100)
          Result.success(BookmarkList("test", emptyList(), 0L))
        }

    viewModel.loadBookmarks()
    testScheduler.runCurrent()

    val state = viewModel.uiState.first()
    assertTrue("isLoading should be true during loading", state.isLoading)
  }

  @Test
  fun `loadBookmarks should store bookmarks in allBookmarks`() = runTest {
    val testUser = User("npub1test")
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
    assertFalse("isLoading should be false after loading", state.isLoading)
    assertEquals("allBookmarks should be set", testBookmarks, state.allBookmarks)
    assertNull("error should be null on success", state.error)
  }

  @Test
  fun `loadBookmarks should set error when not logged in`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    viewModel.loadBookmarks()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse("isLoading should be false", state.isLoading)
    assertEquals("error should indicate not logged in", "Not logged in", state.error)
    assertTrue("bookmarks should be empty", state.bookmarks.isEmpty())
  }

  @Test
  fun `loadBookmarks should set error on failure`() = runTest {
    val testUser = User("npub1test")
    val errorMessage = "Network error"
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any()) } returns Result.failure(Exception(errorMessage))

    viewModel.loadBookmarks()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse("isLoading should be false", state.isLoading)
    assertEquals("error should be set", errorMessage, state.error)
  }

  @Test
  fun `refresh should call loadBookmarks`() = runTest {
    val testUser = User("npub1test")
    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { getBookmarkListUseCase(any()) } returns
        Result.success(BookmarkList("test", emptyList(), 0L))

    viewModel.refresh()
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
    assertNotNull("Dialog should be shown", state.selectedBookmarkForUrlDialog)

    viewModel.dismissUrlDialog()
    advanceUntilIdle()

    state = viewModel.uiState.first()
    assertNull("Dialog should be dismissed", state.selectedBookmarkForUrlDialog)
  }

  @Test
  fun `error dialog workflow should work correctly`() = runTest {
    viewModel.setUrlOpenError("Test error")
    advanceUntilIdle()

    var state = viewModel.uiState.first()
    assertNotNull("Error should be shown", state.urlOpenError)

    viewModel.dismissErrorDialog()
    advanceUntilIdle()

    state = viewModel.uiState.first()
    assertNull("Error should be dismissed", state.urlOpenError)
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
    assertNotNull("URL dialog should be shown", state.selectedBookmarkForUrlDialog)
    assertNotNull("Error dialog should be shown", state.urlOpenError)

    viewModel.dismissUrlDialog()
    advanceUntilIdle()

    val stateAfterDismissUrl = viewModel.uiState.first()
    assertNull("URL dialog should be dismissed", stateAfterDismissUrl.selectedBookmarkForUrlDialog)
    assertNotNull("Error dialog should still be shown", stateAfterDismissUrl.urlOpenError)
  }
}
