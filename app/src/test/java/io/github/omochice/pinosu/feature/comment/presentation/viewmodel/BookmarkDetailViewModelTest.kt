package io.github.omochice.pinosu.feature.comment.presentation.viewmodel

import android.content.Intent
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.core.nip.nip55.SignedEventResponse
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import io.github.omochice.pinosu.feature.comment.domain.usecase.GetCommentsForBookmarkUseCase
import io.github.omochice.pinosu.feature.comment.domain.usecase.PostCommentUseCase
import io.mockk.coEvery
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Unit tests for [BookmarkDetailViewModel] */
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkDetailViewModelTest {

  private lateinit var getCommentsUseCase: GetCommentsForBookmarkUseCase
  private lateinit var postCommentUseCase: PostCommentUseCase
  private lateinit var nip55SignerClient: Nip55SignerClient
  private lateinit var viewModel: BookmarkDetailViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    getCommentsUseCase = mockk(relaxed = true)
    postCommentUseCase = mockk(relaxed = true)
    nip55SignerClient = mockk(relaxed = true)
    viewModel = BookmarkDetailViewModel(getCommentsUseCase, postCommentUseCase, nip55SignerClient)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state has isLoading false and empty comments`() = runTest {
    val state = viewModel.uiState.first()

    assertFalse(state.isLoading)
    assertTrue(state.comments.isEmpty())
    assertEquals("", state.commentInput)
    assertNull(state.error)
    assertFalse(state.postSuccess)
  }

  @Test
  fun `loadComments populates comments from use case`() = runTest {
    val comments =
        listOf(
            Comment(
                id = "c1",
                content = "Hello",
                authorPubkey = "p1",
                createdAt = 1_700_000_000L,
                isAuthorComment = false))

    coEvery {
      getCommentsUseCase(
          rootPubkey = "author-pk",
          dTag = "example.com",
          rootEventId = "evt-1",
          authorContent = "My note",
          authorCreatedAt = 1_699_999_999L)
    } returns Result.success(comments)

    viewModel.loadComments(
        rootPubkey = "author-pk",
        dTag = "example.com",
        rootEventId = "evt-1",
        authorContent = "My note",
        authorCreatedAt = 1_699_999_999L)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.isLoading)
    assertEquals(1, state.comments.size)
    assertEquals("Hello", state.comments[0].content)
  }

  @Test
  fun `loadComments sets error on failure`() = runTest {
    coEvery { getCommentsUseCase(any(), any(), any(), any(), any()) } returns
        Result.failure(RuntimeException("Network error"))

    viewModel.loadComments(
        rootPubkey = "pk", dTag = "d", rootEventId = "e", authorContent = "", authorCreatedAt = 0L)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.isLoading)
    assertNotNull(state.error)
  }

  @Test
  fun `author content appears as first comment`() = runTest {
    val authorComment =
        Comment(
            id = "author-content-evt-1",
            content = "Author note",
            authorPubkey = "author-pk",
            createdAt = 1_699_999_999L,
            isAuthorComment = true)
    val relayComment =
        Comment(
            id = "c1",
            content = "Reply",
            authorPubkey = "other-pk",
            createdAt = 1_700_000_000L,
            isAuthorComment = false)

    coEvery { getCommentsUseCase(any(), any(), any(), any(), any()) } returns
        Result.success(listOf(authorComment, relayComment))

    viewModel.loadComments(
        rootPubkey = "author-pk",
        dTag = "example.com",
        rootEventId = "evt-1",
        authorContent = "Author note",
        authorCreatedAt = 1_699_999_999L)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(2, state.comments.size)
    assertTrue(state.comments[0].isAuthorComment)
    assertFalse(state.comments[1].isAuthorComment)
  }

  @Test
  fun `updateCommentInput updates commentInput in state`() = runTest {
    viewModel.updateCommentInput("New comment text")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("New comment text", state.commentInput)
  }

  @Test
  fun `prepareSignCommentIntent creates Intent via callback`() = runTest {
    val unsignedEvent =
        UnsignedNostrEvent(
            pubkey = "my-pk",
            createdAt = 1_700_000_000L,
            kind = 1111,
            tags = emptyList(),
            content = "My comment")
    val mockIntent = mockk<Intent>()

    coEvery {
      postCommentUseCase.createUnsignedEvent(
          content = "My comment",
          rootPubkey = "root-pk",
          dTag = "example.com",
          rootEventId = "evt-1")
    } returns Result.success(unsignedEvent)
    every { nip55SignerClient.createSignEventIntent(any()) } returns mockIntent

    viewModel.updateCommentInput("My comment")
    advanceUntilIdle()

    var receivedIntent: Intent? = null
    viewModel.prepareSignCommentIntent(
        rootPubkey = "root-pk", dTag = "example.com", rootEventId = "evt-1") {
          receivedIntent = it
        }
    advanceUntilIdle()

    assertEquals(mockIntent, receivedIntent)
  }

  @Test
  fun `processSignedComment publishes and refreshes on success`() = runTest {
    val signedJson = """{"id":"evt-signed","sig":"abc"}"""
    val publishResult = PublishResult("evt-signed", listOf("wss://relay.test.com"), emptyList())
    val mockIntent = mockk<Intent>()

    every { nip55SignerClient.handleSignEventResponse(any(), any()) } returns
        Result.success(SignedEventResponse(signedJson))
    coEvery { postCommentUseCase.publishSignedEvent(any()) } returns Result.success(publishResult)

    viewModel.processSignedComment(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue(state.postSuccess)
    assertFalse(state.isSubmitting)
  }

  @Test
  fun `processSignedComment sets error on failure`() = runTest {
    val mockIntent = mockk<Intent>()

    every { nip55SignerClient.handleSignEventResponse(any(), any()) } returns
        Result.failure(RuntimeException("Signing failed"))

    viewModel.processSignedComment(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.error)
    assertFalse(state.isSubmitting)
  }
}
