package io.github.omochice.pinosu.presentation.viewmodel

import android.content.Intent
import io.github.omochice.pinosu.data.model.UnsignedNostrEvent
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.data.nip55.SignedEventResponse
import io.github.omochice.pinosu.data.relay.PublishResult
import io.github.omochice.pinosu.domain.usecase.PostBookmarkUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Unit tests for [PostBookmarkViewModel] */
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PostBookmarkViewModelTest {

  private lateinit var postBookmarkUseCase: PostBookmarkUseCase
  private lateinit var nip55SignerClient: Nip55SignerClient
  private lateinit var viewModel: PostBookmarkViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    postBookmarkUseCase = mockk(relaxed = true)
    nip55SignerClient = mockk(relaxed = true)
    viewModel = PostBookmarkViewModel(postBookmarkUseCase, nip55SignerClient)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state should have default values`() = runTest {
    val state = viewModel.uiState.first()

    assertEquals("url should be empty", "", state.url)
    assertEquals("title should be empty", "", state.title)
    assertEquals("categories should be empty", "", state.categories)
    assertEquals("comment should be empty", "", state.comment)
    assertFalse("isSubmitting should be false", state.isSubmitting)
    assertNull("errorMessage should be null", state.errorMessage)
    assertFalse("postSuccess should be false", state.postSuccess)
    assertNull("unsignedEventJson should be null", state.unsignedEventJson)
  }

  @Test
  fun `updateUrl should strip https scheme`() = runTest {
    viewModel.updateUrl("https://example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("URL should have https:// stripped", "example.com/path", state.url)
  }

  @Test
  fun `updateUrl should strip http scheme`() = runTest {
    viewModel.updateUrl("http://example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("URL should have http:// stripped", "example.com/path", state.url)
  }

  @Test
  fun `updateUrl should strip HTTPS uppercase scheme`() = runTest {
    viewModel.updateUrl("HTTPS://example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("URL should have HTTPS:// stripped", "example.com/path", state.url)
  }

  @Test
  fun `updateUrl should strip HTTP uppercase scheme`() = runTest {
    viewModel.updateUrl("HTTP://example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("URL should have HTTP:// stripped", "example.com/path", state.url)
  }

  @Test
  fun `updateUrl should keep URL without scheme unchanged`() = runTest {
    viewModel.updateUrl("example.com/path")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("URL without scheme should remain unchanged", "example.com/path", state.url)
  }

  @Test
  fun `updateTitle should update title`() = runTest {
    viewModel.updateTitle("Test Title")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("title should be updated", "Test Title", state.title)
  }

  @Test
  fun `updateCategories should update categories`() = runTest {
    viewModel.updateCategories("tech, kotlin, android")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("categories should be updated", "tech, kotlin, android", state.categories)
  }

  @Test
  fun `updateComment should update comment`() = runTest {
    viewModel.updateComment("Test comment")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("comment should be updated", "Test comment", state.comment)
  }

  @Test
  fun `dismissError should clear error message`() = runTest {
    viewModel.updateUrl("")
    viewModel.prepareSignEventIntent {}
    advanceUntilIdle()

    viewModel.dismissError()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNull("errorMessage should be null", state.errorMessage)
  }

  @Test
  fun `resetPostSuccess should set postSuccess to false`() = runTest {
    viewModel.resetPostSuccess()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse("postSuccess should be false", state.postSuccess)
  }

  @Test
  fun `prepareSignEventIntent should set error when URL is blank`() = runTest {
    viewModel.updateUrl("")
    advanceUntilIdle()

    var receivedIntent: Intent? = Intent()
    viewModel.prepareSignEventIntent { receivedIntent = it }
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull("errorMessage should be set", state.errorMessage)
    assertNull("callback should receive null", receivedIntent)
  }

  @Test
  fun `prepareSignEventIntent should call useCase and create intent when URL is valid`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1234567890,
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
    assertEquals("callback should receive the intent", mockIntent, receivedIntent)
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
    assertNotNull("errorMessage should be set", state.errorMessage)
    assertFalse("isSubmitting should be false", state.isSubmitting)
  }

  @Test
  fun `processSignedEvent should publish event on success`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1234567890,
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
    assertTrue("postSuccess should be true", state.postSuccess)
    assertFalse("isSubmitting should be false", state.isSubmitting)
  }

  @Test
  fun `processSignedEvent should set error on signer failure`() = runTest {
    val mockIntent = mockk<Intent>()

    every { nip55SignerClient.handleSignEventResponse(any(), any()) } returns
        Result.failure(RuntimeException("Signing failed"))

    viewModel.processSignedEvent(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull("errorMessage should be set", state.errorMessage)
    assertFalse("isSubmitting should be false", state.isSubmitting)
    assertFalse("postSuccess should be false", state.postSuccess)
  }

  @Test
  fun `processSignedEvent should set error on publish failure`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1234567890,
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
    assertNotNull("errorMessage should be set", state.errorMessage)
    assertFalse("isSubmitting should be false", state.isSubmitting)
    assertFalse("postSuccess should be false", state.postSuccess)
  }

  @Test
  fun `prepareSignEventIntent should parse categories correctly`() = runTest {
    val realEvent =
        UnsignedNostrEvent(
            pubkey = "abc123def456abc123def456abc123def456abc123def456abc123def456abc1",
            createdAt = 1234567890,
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
