package io.github.omochice.pinosu.presentation.viewmodel

import android.app.Activity
import android.content.Intent
import io.github.omochice.pinosu.data.model.UnsignedNostrEvent
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.PostBookmarkUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
 * Unit tests for PostBookmarkViewModel
 *
 * Tests cover:
 * - Form field updates (URL, title, categories, comment)
 * - URL scheme removal
 * - Category parsing (comma-separated, trim, empty filter)
 * - NIP-55 signing flow (success, cancel, error)
 * - Error message display and dismissal
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostBookmarkViewModelTest {

  private lateinit var postBookmarkUseCase: PostBookmarkUseCase
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var nip55SignerClient: Nip55SignerClient
  private lateinit var viewModel: PostBookmarkViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    postBookmarkUseCase = mockk(relaxed = true)
    getLoginStateUseCase = mockk(relaxed = true)
    nip55SignerClient = mockk(relaxed = true)
    viewModel = PostBookmarkViewModel(postBookmarkUseCase, getLoginStateUseCase, nip55SignerClient)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ========== Form Update Tests ==========

  @Test
  fun `initial PostBookmarkUiState should have default values`() = runTest {
    val state = viewModel.uiState.first()

    assertEquals("url should be empty", "", state.url)
    assertEquals("title should be empty", "", state.title)
    assertEquals("categories should be empty", "", state.categories)
    assertEquals("comment should be empty", "", state.comment)
    assertFalse("isSubmitting should be false", state.isSubmitting)
    assertNull("errorMessage should be null", state.errorMessage)
    assertFalse("postSuccess should be false", state.postSuccess)
    assertNull("unsignedEventJson should be null", state.unsignedEventJson)
    assertNull("unsignedEvent should be null", state.unsignedEvent)
    assertFalse("readyToSign should be false", state.readyToSign)
  }

  @Test
  fun `updateUrl should update url field`() = runTest {
    val testUrl = "https://example.com"

    viewModel.updateUrl(testUrl)

    val state = viewModel.uiState.first()
    assertEquals("url should be updated", testUrl, state.url)
  }

  @Test
  fun `updateUrl should clear errorMessage`() = runTest {
    viewModel.updateUrl("")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    var state = viewModel.uiState.first()
    assertNotNull("errorMessage should be set", state.errorMessage)

    viewModel.updateUrl("https://example.com")
    state = viewModel.uiState.first()
    assertNull("errorMessage should be cleared", state.errorMessage)
  }

  @Test
  fun `updateTitle should update title field`() = runTest {
    val testTitle = "Test Title"

    viewModel.updateTitle(testTitle)

    val state = viewModel.uiState.first()
    assertEquals("title should be updated", testTitle, state.title)
  }

  @Test
  fun `updateCategories should update categories field`() = runTest {
    val testCategories = "tech, news"

    viewModel.updateCategories(testCategories)

    val state = viewModel.uiState.first()
    assertEquals("categories should be updated", testCategories, state.categories)
  }

  @Test
  fun `updateComment should update comment field`() = runTest {
    val testComment = "This is a test comment"

    viewModel.updateComment(testComment)

    val state = viewModel.uiState.first()
    assertEquals("comment should be updated", testComment, state.comment)
  }

  // ========== URL Validation Tests ==========

  @Test
  fun `prepareAndSignEvent with blank URL should show error`() = runTest {
    viewModel.updateUrl("")

    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("errorMessage should indicate URL is required", "URLを入力してください", state.errorMessage)
    assertFalse("isSubmitting should be false", state.isSubmitting)
  }

  @Test
  fun `prepareAndSignEvent with whitespace URL should show error`() = runTest {
    viewModel.updateUrl("   ")

    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("errorMessage should indicate URL is required", "URLを入力してください", state.errorMessage)
  }

  // ========== Category Parsing Tests ==========

  @Test
  fun `prepareAndSignEvent should parse comma-separated categories`() = runTest {
    val testUser = User("npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9")
    val testUnsignedEvent = createTestUnsignedEvent()

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any(), any()) } returns
        Result.success(testUnsignedEvent)

    viewModel.updateUrl("https://example.com")
    viewModel.updateCategories("tech, news, programming")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    coVerify {
      postBookmarkUseCase.createUnsignedEvent(
          any(), any(), any(), match { it == listOf("tech", "news", "programming") }, any())
    }
  }

  @Test
  fun `prepareAndSignEvent should trim whitespace from categories`() = runTest {
    val testUser = User("npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9")
    val testUnsignedEvent = createTestUnsignedEvent()

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any(), any()) } returns
        Result.success(testUnsignedEvent)

    viewModel.updateUrl("https://example.com")
    viewModel.updateCategories("  tech  ,  news  ,  programming  ")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    coVerify {
      postBookmarkUseCase.createUnsignedEvent(
          any(), any(), any(), match { it == listOf("tech", "news", "programming") }, any())
    }
  }

  @Test
  fun `prepareAndSignEvent should filter empty categories`() = runTest {
    val testUser = User("npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9")
    val testUnsignedEvent = createTestUnsignedEvent()

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any(), any()) } returns
        Result.success(testUnsignedEvent)

    viewModel.updateUrl("https://example.com")
    viewModel.updateCategories("tech,,news,  ,programming")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    coVerify {
      postBookmarkUseCase.createUnsignedEvent(
          any(), any(), any(), match { it == listOf("tech", "news", "programming") }, any())
    }
  }

  @Test
  fun `prepareAndSignEvent with empty categories should pass empty list`() = runTest {
    val testUser = User("npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9")
    val testUnsignedEvent = createTestUnsignedEvent()

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any(), any()) } returns
        Result.success(testUnsignedEvent)

    viewModel.updateUrl("https://example.com")
    viewModel.updateCategories("")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    coVerify {
      postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), match { it.isEmpty() }, any())
    }
  }

  // ========== NIP-55 Signing Flow Tests ==========

  @Test
  fun `prepareAndSignEvent success should set readyToSign and unsignedEvent`() = runTest {
    val testUser = User("npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9")
    val testUnsignedEvent = createTestUnsignedEvent()

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any(), any()) } returns
        Result.success(testUnsignedEvent)

    viewModel.updateUrl("https://example.com")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue("readyToSign should be true", state.readyToSign)
    assertNotNull("unsignedEventJson should be set", state.unsignedEventJson)
    assertNotNull("unsignedEvent should be set", state.unsignedEvent)
    assertFalse("isSubmitting should be false after prepare", state.isSubmitting)
    assertNull("errorMessage should be null on success", state.errorMessage)
  }

  @Test
  fun `prepareAndSignEvent when not logged in should show error`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    viewModel.updateUrl("https://example.com")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("errorMessage should indicate login required", "ログインが必要です", state.errorMessage)
    assertFalse("isSubmitting should be false", state.isSubmitting)
  }

  @Test
  fun `prepareAndSignEvent when event creation fails should show error`() = runTest {
    val testUser = User("npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9")

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any(), any()) } returns
        Result.failure(Exception("Failed"))

    viewModel.updateUrl("https://example.com")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(
        "errorMessage should indicate creation failure", "イベント作成に失敗しました", state.errorMessage)
    assertFalse("isSubmitting should be false", state.isSubmitting)
  }

  @Test
  fun `createSignEventIntent should return null when unsignedEventJson is null`() = runTest {
    val intent = viewModel.createSignEventIntent()

    assertNull("intent should be null when unsignedEventJson is null", intent)
    val state = viewModel.uiState.first()
    assertEquals(
        "errorMessage should indicate event not created", "イベントが作成されていません", state.errorMessage)
  }

  @Test
  fun `processSignedEvent with RESULT_CANCELED should show error`() = runTest {
    viewModel.processSignedEvent(Activity.RESULT_CANCELED, null)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("errorMessage should indicate cancellation", "署名がキャンセルされました", state.errorMessage)
    assertFalse("isSubmitting should be false", state.isSubmitting)
  }

  @Test
  fun `processSignedEvent with null data should show error`() = runTest {
    viewModel.processSignedEvent(Activity.RESULT_OK, null)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(
        "errorMessage should indicate construction failure",
        "署名済みイベントの構築に失敗しました",
        state.errorMessage)
    assertFalse("isSubmitting should be false", state.isSubmitting)
  }

  @Test
  fun `processSignedEvent with rejected=true should show error`() = runTest {
    val data = mockk<Intent>(relaxed = true)
    every { data.getBooleanExtra("rejected", false) } returns true

    viewModel.processSignedEvent(Activity.RESULT_OK, data)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("errorMessage should indicate cancellation", "署名がキャンセルされました", state.errorMessage)
  }

  @Test
  fun `processSignedEvent with complete signed JSON should publish successfully`() = runTest {
    val testUser = User("npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9")
    val testUnsignedEvent = createTestUnsignedEvent()
    val signedEventJson =
        """{"id":"abc123","pubkey":"testpubkey","created_at":1234567890,"kind":39701,"tags":[],"content":"","sig":"testsig"}"""

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any(), any()) } returns
        Result.success(testUnsignedEvent)
    coEvery { postBookmarkUseCase.publishSignedEvent(any()) } returns Result.success(Unit)

    viewModel.updateUrl("https://example.com")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    val data = mockk<Intent>(relaxed = true)
    every { data.getBooleanExtra("rejected", false) } returns false
    every { data.getStringExtra("result") } returns signedEventJson

    viewModel.processSignedEvent(Activity.RESULT_OK, data)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue("postSuccess should be true", state.postSuccess)
    assertFalse("isSubmitting should be false", state.isSubmitting)
    assertNull("errorMessage should be null", state.errorMessage)

    coVerify { postBookmarkUseCase.publishSignedEvent(any()) }
  }

  @Test
  fun `processSignedEvent with signature only should build signed event and publish`() = runTest {
    val testUser = User("npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9")
    val testUnsignedEvent = createTestUnsignedEvent()
    val signature = "abc123signature"

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any(), any()) } returns
        Result.success(testUnsignedEvent)
    coEvery { postBookmarkUseCase.publishSignedEvent(any()) } returns Result.success(Unit)

    viewModel.updateUrl("https://example.com")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    val data = mockk<Intent>(relaxed = true)
    every { data.getBooleanExtra("rejected", false) } returns false
    every { data.getStringExtra("result") } returns signature

    viewModel.processSignedEvent(Activity.RESULT_OK, data)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue("postSuccess should be true", state.postSuccess)

    coVerify { postBookmarkUseCase.publishSignedEvent(any()) }
  }

  @Test
  fun `processSignedEvent with publish failure should show error`() = runTest {
    val testUser = User("npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9")
    val testUnsignedEvent = createTestUnsignedEvent()
    val signedEventJson =
        """{"id":"abc123","pubkey":"testpubkey","created_at":1234567890,"kind":39701,"tags":[],"content":"","sig":"testsig"}"""

    coEvery { getLoginStateUseCase() } returns testUser
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any(), any()) } returns
        Result.success(testUnsignedEvent)
    coEvery { postBookmarkUseCase.publishSignedEvent(any()) } returns
        Result.failure(Exception("Network error"))

    viewModel.updateUrl("https://example.com")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    val data = mockk<Intent>(relaxed = true)
    every { data.getBooleanExtra("rejected", false) } returns false
    every { data.getStringExtra("result") } returns signedEventJson

    viewModel.processSignedEvent(Activity.RESULT_OK, data)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(
        "errorMessage should indicate publish failure", "ブックマークの投稿に失敗しました", state.errorMessage)
    assertFalse("postSuccess should be false", state.postSuccess)
  }

  // ========== Error Handling Tests ==========

  @Test
  fun `dismissError should clear errorMessage`() = runTest {
    viewModel.updateUrl("")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    var state = viewModel.uiState.first()
    assertNotNull("errorMessage should be set", state.errorMessage)

    viewModel.dismissError()
    state = viewModel.uiState.first()
    assertNull("errorMessage should be cleared after dismissError", state.errorMessage)
  }

  @Test
  fun `error message workflow should work correctly`() = runTest {
    viewModel.updateUrl("")
    viewModel.prepareAndSignEvent()
    advanceUntilIdle()

    var state = viewModel.uiState.first()
    assertEquals("Error should be shown", "URLを入力してください", state.errorMessage)

    viewModel.dismissError()
    state = viewModel.uiState.first()
    assertNull("Error should be dismissed", state.errorMessage)
  }

  // ========== Helper Methods ==========

  private fun createTestUnsignedEvent(): UnsignedNostrEvent {
    return UnsignedNostrEvent(
        pubkey = "testpubkey",
        createdAt = 1234567890,
        kind = 39701,
        tags = listOf(listOf("d", "example.com"), listOf("r", "https://example.com")),
        content = "Test comment")
  }
}
