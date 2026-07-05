package io.github.omochice.pinosu.feature.postbookmark.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.core.nip.nip55.SignedEventResponse
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.postbookmark.domain.usecase.PostBookmarkUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Unit tests for [PostBookmarkViewModel] */
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PostBookmarkViewModelTest {

  private lateinit var postBookmarkUseCase: PostBookmarkUseCase
  private lateinit var nip55SignerClient: Nip55SignerClient
  private lateinit var savedStateHandle: SavedStateHandle
  private lateinit var viewModel: PostBookmarkViewModel

  private val testDispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    postBookmarkUseCase = mockk(relaxed = true)
    nip55SignerClient = mockk(relaxed = true)
    savedStateHandle = SavedStateHandle()
    viewModel = PostBookmarkViewModel(postBookmarkUseCase, nip55SignerClient, savedStateHandle)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state should have default values`() = runTest {
    val state = viewModel.uiState.first()

    assertEquals("", state.url, "url should be empty")
    assertEquals("", state.title, "title should be empty")
    assertEquals("", state.categories, "categories should be empty")
    assertEquals("", state.comment, "comment should be empty")
    assertFalse(state.isSubmitting, "isSubmitting should be false")
    assertNull(state.errorMessage, "errorMessage should be null")
    assertFalse(state.postSuccess, "postSuccess should be false")
  }

  @Test
  fun `updateUrl should strip https scheme`() = runTest {
    viewModel.updateUrl("https://example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("example.com/path", state.url, "URL should have https:// stripped")
  }

  @Test
  fun `updateUrl should strip http scheme`() = runTest {
    viewModel.updateUrl("http://example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("example.com/path", state.url, "URL should have http:// stripped")
  }

  @Test
  fun `updateUrl should strip HTTPS uppercase scheme`() = runTest {
    viewModel.updateUrl("HTTPS://example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("example.com/path", state.url, "URL should have HTTPS:// stripped")
  }

  @Test
  fun `updateUrl should strip HTTP uppercase scheme`() = runTest {
    viewModel.updateUrl("HTTP://example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("example.com/path", state.url, "URL should have HTTP:// stripped")
  }

  @Test
  fun `updateUrl should keep URL without scheme unchanged`() = runTest {
    viewModel.updateUrl("example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("example.com/path", state.url, "URL without scheme should remain unchanged")
  }

  @Test
  fun `updateTitle should update title`() = runTest {
    viewModel.updateTitle("Test Title")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("Test Title", state.title, "title should be updated")
  }

  @Test
  fun `updateCategories should update categories`() = runTest {
    viewModel.updateCategories("tech, kotlin, android")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("tech, kotlin, android", state.categories, "categories should be updated")
  }

  @Test
  fun `updateComment should update comment`() = runTest {
    viewModel.updateComment("Test comment")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("Test comment", state.comment, "comment should be updated")
  }

  @Test
  fun `dismissError should clear error message`() = runTest {
    viewModel.updateUrl("")
    viewModel.prepareSignEventIntent {}
    advanceUntilIdle()

    viewModel.dismissError()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNull(state.errorMessage, "errorMessage should be null")
  }

  @Test
  fun `prepareSignEventIntent should set error when URL is blank`() = runTest {
    viewModel.updateUrl("")
    advanceUntilIdle()

    var receivedIntent: Intent? = Intent()
    viewModel.prepareSignEventIntent { receivedIntent = it }
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.errorMessage, "errorMessage should be set")
    assertNull(receivedIntent, "callback should receive null")
  }

  @Test
  fun `prepareSignEventIntent should call useCase and create intent when URL is valid`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1_234_567_890,
            kind = 39701,
            tags = listOf(listOf("d", "example.com")),
            content = "Test comment")
    val mockIntent = mockk<Intent>()

    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any()) } returns
        Result.success(realEvent)
    every { nip55SignerClient.createSignEventIntent(any()) } returns mockIntent

    viewModel.updateUrl("example.com")
    viewModel.updateTitle("Test")
    viewModel.updateCategories("tech, kotlin")
    viewModel.updateComment("Test comment")
    advanceUntilIdle()

    var receivedIntent: Intent? = null
    viewModel.prepareSignEventIntent { receivedIntent = it }
    advanceUntilIdle()

    coVerify {
      postBookmarkUseCase.createUnsignedEvent("example.com", "Test", any(), "Test comment")
    }
    verify { nip55SignerClient.createSignEventIntent(any()) }
    assertEquals(mockIntent, receivedIntent, "callback should receive the intent")
  }

  @Test
  fun `prepareSignEventIntent should set error on useCase failure`() = runTest {
    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any()) } returns
        Result.failure(RuntimeException("Test error"))

    viewModel.updateUrl("example.com")
    advanceUntilIdle()

    viewModel.prepareSignEventIntent {}
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.errorMessage, "errorMessage should be set")
    assertFalse(state.isSubmitting, "isSubmitting should be false")
  }

  @Test
  fun `processSignedEvent should publish event on success`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1_234_567_890,
            kind = 39701,
            tags = listOf(listOf("d", "example.com")),
            content = "comment")
    val signedEventJson = """{"id":"event123","pubkey":"abc","sig":"xyz"}"""
    val mockPublishResult =
        PublishResult("event123", listOf("wss://relay.example.com"), emptyList())
    val mockIntent = mockk<Intent>()

    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any()) } returns
        Result.success(realEvent)
    every { nip55SignerClient.createSignEventIntent(any()) } returns mockIntent
    every { nip55SignerClient.handleSignEventResponse(any(), any()) } returns
        Result.success(SignedEventResponse(signedEventJson))
    coEvery { postBookmarkUseCase.publishSignedEvent(any()) } returns
        Result.success(mockPublishResult)

    viewModel.updateUrl("example.com")
    advanceUntilIdle()

    viewModel.prepareSignEventIntent {}
    advanceUntilIdle()

    viewModel.processSignedEvent(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue(state.postSuccess, "postSuccess should be true")
    assertFalse(state.isSubmitting, "isSubmitting should be false")
  }

  @Test
  fun `pending event survives process death and publishes signature-only response`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1_234_567_890,
            kind = 39701,
            tags = listOf(listOf("d", "example.com")),
            content = "comment")
    val signatureOnlyResponse = "a".repeat(128)
    val mockIntent = mockk<Intent>()

    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any()) } returns
        Result.success(realEvent)
    every { nip55SignerClient.createSignEventIntent(any()) } returns mockIntent
    every { nip55SignerClient.handleSignEventResponse(any(), any()) } returns
        Result.success(SignedEventResponse(signatureOnlyResponse))
    coEvery { postBookmarkUseCase.publishSignedEvent(any()) } returns
        Result.success(PublishResult("event123", listOf("wss://relay.example.com"), emptyList()))

    viewModel.updateUrl("example.com")
    advanceUntilIdle()
    viewModel.prepareSignEventIntent {}
    advanceUntilIdle()

    // Simulate process death while the signer is foregrounded: the original ViewModel is destroyed
    // and recreated from the same SavedStateHandle. The pending event must be restored so a
    // signature-only response can still be assembled into a full signed event.
    val revivedViewModel =
        PostBookmarkViewModel(postBookmarkUseCase, nip55SignerClient, savedStateHandle)

    revivedViewModel.processSignedEvent(-1, mockIntent)
    advanceUntilIdle()

    val state = revivedViewModel.uiState.first()
    assertTrue(state.postSuccess, "revived ViewModel should rebuild and publish the signed event")
    assertNull(state.errorMessage, "no build failure should be reported after process death")
    coVerify { postBookmarkUseCase.publishSignedEvent(any()) }
  }

  @Test
  fun `processSignedEvent should set error on signer failure`() = runTest {
    val mockIntent = mockk<Intent>()

    every { nip55SignerClient.handleSignEventResponse(any(), any()) } returns
        Result.failure(RuntimeException("Signing failed"))

    viewModel.processSignedEvent(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.errorMessage, "errorMessage should be set")
    assertFalse(state.isSubmitting, "isSubmitting should be false")
    assertFalse(state.postSuccess, "postSuccess should be false")
  }

  @Test
  fun `processSignedEvent should set error on publish failure`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1_234_567_890,
            kind = 39701,
            tags = listOf(listOf("d", "example.com")),
            content = "comment")
    val signedEventJson = """{"id":"event123","pubkey":"abc","sig":"xyz"}"""
    val mockIntent = mockk<Intent>()

    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any()) } returns
        Result.success(realEvent)
    every { nip55SignerClient.createSignEventIntent(any()) } returns mockIntent
    every { nip55SignerClient.handleSignEventResponse(any(), any()) } returns
        Result.success(SignedEventResponse(signedEventJson))
    coEvery { postBookmarkUseCase.publishSignedEvent(any()) } returns
        Result.failure(RuntimeException("Publish failed"))

    viewModel.updateUrl("example.com")
    advanceUntilIdle()

    viewModel.prepareSignEventIntent {}
    advanceUntilIdle()

    viewModel.processSignedEvent(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.errorMessage, "errorMessage should be set")
    assertFalse(state.isSubmitting, "isSubmitting should be false")
    assertFalse(state.postSuccess, "postSuccess should be false")
  }

  @Test
  fun `initializeForEdit should set edit mode with existing values`() = runTest {
    viewModel.initializeForEdit(
        url = "example.com/article",
        title = "Existing Title",
        categories = listOf("tech", "kotlin"),
        comment = "Existing comment")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue(state.isEditMode, "isEditMode should be true")
    assertEquals("example.com/article", state.url, "url should be set")
    assertEquals("Existing Title", state.title, "title should be set")
    assertEquals("tech, kotlin", state.categories, "categories should be set")
    assertEquals("Existing comment", state.comment, "comment should be set")
  }

  @Test
  fun `resetForm should clear edit mode and all fields`() = runTest {
    viewModel.initializeForEdit(
        url = "example.com/article",
        title = "Title",
        categories = listOf("tech"),
        comment = "Comment")
    advanceUntilIdle()

    viewModel.resetForm()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.isEditMode, "isEditMode should be false")
    assertEquals("", state.url, "url should be empty")
    assertEquals("", state.title, "title should be empty")
    assertEquals("", state.categories, "categories should be empty")
    assertEquals("", state.comment, "comment should be empty")
  }

  @Test
  fun `prepareSignEventIntent in edit mode should use original URL`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1_234_567_890,
            kind = 39701,
            tags = listOf(listOf("d", "example.com/article")),
            content = "Updated comment")
    val mockIntent = mockk<Intent>()

    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any()) } returns
        Result.success(realEvent)
    every { nip55SignerClient.createSignEventIntent(any()) } returns mockIntent

    viewModel.initializeForEdit(
        url = "example.com/article",
        title = "Original Title",
        categories = listOf("tech"),
        comment = "Original comment")
    viewModel.updateTitle("Updated Title")
    viewModel.updateComment("Updated comment")
    advanceUntilIdle()

    viewModel.prepareSignEventIntent {}
    advanceUntilIdle()

    coVerify {
      postBookmarkUseCase.createUnsignedEvent(
          "example.com/article", "Updated Title", any(), "Updated comment")
    }
  }

  @Test
  fun `prepareSignEventIntent should parse categories correctly`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1_234_567_890,
            kind = 39701,
            tags = emptyList(),
            content = "")
    val mockIntent = mockk<Intent>()

    coEvery { postBookmarkUseCase.createUnsignedEvent(any(), any(), any(), any()) } returns
        Result.success(realEvent)
    every { nip55SignerClient.createSignEventIntent(any()) } returns mockIntent

    viewModel.updateUrl("example.com")
    viewModel.updateCategories("  tech  ,  kotlin  ,  ,  android  ")
    advanceUntilIdle()

    viewModel.prepareSignEventIntent {}
    advanceUntilIdle()

    coVerify {
      postBookmarkUseCase.createUnsignedEvent(
          any(), any(), match { it == listOf("tech", "kotlin", "android") }, any())
    }
  }
}
