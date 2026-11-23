package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip55.Nip55Error
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.core.nip.nip55.SignedEventResponse
import io.github.omochice.pinosu.core.nip.nip78.Nip78
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.feed.domain.model.FeedCandidate
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionChange
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.model.error.HttpFeedNotSupportedException
import io.github.omochice.pinosu.feature.feed.domain.model.error.NoFeedFoundException
import io.github.omochice.pinosu.feature.feed.domain.model.error.ReadOnlyLoginException
import io.github.omochice.pinosu.feature.feed.domain.usecase.LoadFeedSubscriptionsUseCase
import io.github.omochice.pinosu.feature.feed.domain.usecase.PrepareFeedSubscriptionChangeUseCase
import io.github.omochice.pinosu.feature.feed.domain.usecase.PublishFeedSubscriptionListUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FeedSubscriptionsViewModelTest {

  private lateinit var loadSubscriptions: LoadFeedSubscriptionsUseCase
  private lateinit var prepareChange: PrepareFeedSubscriptionChangeUseCase
  private lateinit var publishList: PublishFeedSubscriptionListUseCase
  private lateinit var nip55SignerClient: Nip55SignerClient
  private lateinit var savedStateHandle: SavedStateHandle

  private val testDispatcher = StandardTestDispatcher()
  private val signIntent = Intent("sign")
  private val event =
      UnsignedNostrEvent(
          pubkey = HEX_PUBKEY,
          createdAt = 1_700_000_000L,
          kind = Nip78.KIND_APP_DATA,
          tags = listOf(listOf("d", "pinosu/rss-subscriptions"), listOf("url", FEED_A)),
          content = "")

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    loadSubscriptions = mockk()
    prepareChange = mockk()
    publishList = mockk()
    nip55SignerClient = mockk()
    savedStateHandle = SavedStateHandle()
    coEvery { loadSubscriptions() } returns Result.success(listOf(FEED_A))
    every { nip55SignerClient.createSignEventIntent(any()) } returns signIntent
    coEvery { publishList(any()) } returns
        Result.success(PublishResult("id", listOf(RELAY), emptyList()))
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel(handle: SavedStateHandle = savedStateHandle) =
      FeedSubscriptionsViewModel(
          loadSubscriptions,
          prepareChange,
          FeedSubscriptionPublisher(nip55SignerClient, publishList, handle))

  private fun TestScope.collectSignRequests(viewModel: FeedSubscriptionsViewModel): List<Intent> {
    val requests = mutableListOf<Intent>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.signRequests.collect { requests.add(it) }
    }
    return requests
  }

  private fun givenSignerReturns(signature: String) {
    every { nip55SignerClient.handleSignEventResponse(Activity.RESULT_OK, any()) } returns
        Result.success(SignedEventResponse(signature))
  }

  private fun readyToSign(isFirstList: Boolean = false) =
      Result.success<FeedSubscriptionChange>(
          FeedSubscriptionChange.ReadyToSign(event, listOf(FEED_A), isFirstList))

  @Test
  fun `loads the subscribed feeds on open`() = runTest {
    val viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(
        FeedSubscriptionsUiState.Content.Loaded(listOf(FEED_A)), viewModel.uiState.value.content)
  }

  @Test
  fun `a list that no relay serves is shown as an error`() = runTest {
    coEvery { loadSubscriptions() } returns
        Result.failure(
            FeedSubscriptionListUnavailableException(FeedSubscriptionLoadResult.Reason.UNREACHABLE))

    val viewModel = createViewModel()
    advanceUntilIdle()

    assertEquals(
        FeedSubscriptionsUiState.Content.Error(FeedLoadError.Unreachable),
        viewModel.uiState.value.content)
  }

  @Test
  fun `adding a feed asks the signer to sign the new list`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns readyToSign()
    val viewModel = createViewModel()
    val requests = collectSignRequests(viewModel)
    advanceUntilIdle()

    viewModel.add(INPUT)
    advanceUntilIdle()

    assertEquals(listOf(signIntent), requests)
    verify { nip55SignerClient.createSignEventIntent(event.toJson()) }
    assertTrue(viewModel.uiState.value.isWorking, "the screen stays busy while the signer is open")
  }

  @Test
  fun `a page with several feeds offers them and picking one continues the add`() = runTest {
    val candidates = listOf(FeedCandidate(FEED_A, "Posts"), FeedCandidate(FEED_B, null))
    coEvery { prepareChange.add(INPUT) } returns
        Result.success(FeedSubscriptionChange.ChooseCandidate(candidates))
    coEvery { prepareChange.add(FEED_A) } returns readyToSign()
    val viewModel = createViewModel()
    val requests = collectSignRequests(viewModel)
    advanceUntilIdle()

    viewModel.add(INPUT)
    advanceUntilIdle()

    assertEquals(
        FeedSubscriptionsDialog.ChooseCandidate(candidates), viewModel.uiState.value.dialog)
    assertFalse(viewModel.uiState.value.isWorking, "the user has to choose before work continues")
    assertTrue(requests.isEmpty(), "nothing is signed before a feed is chosen")

    viewModel.add(FEED_A)
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.dialog)
    assertEquals(listOf(signIntent), requests)
  }

  @Test
  fun `the first list is signed only after the user confirms that it is public`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns readyToSign(isFirstList = true)
    val viewModel = createViewModel()
    val requests = collectSignRequests(viewModel)
    advanceUntilIdle()

    viewModel.add(INPUT)
    advanceUntilIdle()

    assertIs<FeedSubscriptionsDialog.ConfirmPublicList>(viewModel.uiState.value.dialog)
    assertTrue(requests.isEmpty(), "the signer must not open before the notice is confirmed")
    verify(exactly = 0) { nip55SignerClient.createSignEventIntent(any()) }

    viewModel.confirmPublicNotice()
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.dialog)
    assertEquals(listOf(signIntent), requests)
  }

  @Test
  fun `declining the public notice signs nothing`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns readyToSign(isFirstList = true)
    val viewModel = createViewModel()
    val requests = collectSignRequests(viewModel)
    advanceUntilIdle()

    viewModel.add(INPUT)
    advanceUntilIdle()
    viewModel.dismissDialog()
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.dialog)
    assertFalse(viewModel.uiState.value.isWorking)
    assertTrue(requests.isEmpty())
    verify(exactly = 0) { nip55SignerClient.createSignEventIntent(any()) }
  }

  @Test
  fun `adding a feed that is already subscribed says so without signing`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns
        Result.success(FeedSubscriptionChange.AlreadySubscribed(FEED_A))
    val viewModel = createViewModel()
    val requests = collectSignRequests(viewModel)
    advanceUntilIdle()

    viewModel.add(INPUT)
    advanceUntilIdle()

    assertEquals(FeedSubscriptionsMessage.AlreadySubscribed, viewModel.uiState.value.message)
    assertFalse(viewModel.uiState.value.isWorking)
    assertTrue(requests.isEmpty())
  }

  @Test
  fun `a list that cannot be loaded blocks the change with the reason`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns
        Result.failure(
            FeedSubscriptionListUnavailableException(
                FeedSubscriptionLoadResult.Reason.NO_USER_RELAYS))
    val viewModel = createViewModel()
    val requests = collectSignRequests(viewModel)
    advanceUntilIdle()

    viewModel.add(INPUT)
    advanceUntilIdle()

    assertEquals(FeedSubscriptionsMessage.NoUserRelays, viewModel.uiState.value.message)
    assertFalse(viewModel.uiState.value.isWorking)
    assertTrue(requests.isEmpty())
  }

  @Test
  fun `failures to find a feed are explained`() = runTest {
    val cases =
        mapOf(
            NoFeedFoundException(INPUT) to FeedSubscriptionsMessage.NoFeedFound,
            HttpFeedNotSupportedException(INPUT, IOException("refused")) to
                FeedSubscriptionsMessage.HttpFeedNotSupported,
            IOException("offline") to FeedSubscriptionsMessage.ChangeFailed)
    val viewModel = createViewModel()
    advanceUntilIdle()

    cases.forEach { (error, expected) ->
      coEvery { prepareChange.add(INPUT) } returns Result.failure(error)

      viewModel.add(INPUT)
      advanceUntilIdle()

      assertEquals(expected, viewModel.uiState.value.message, "message for $error")
      viewModel.dismissMessage()
    }
  }

  @Test
  fun `a read-only login is refused`() = runTest {
    coEvery { prepareChange.remove(FEED_A) } returns Result.failure(ReadOnlyLoginException())
    val viewModel = createViewModel()
    val requests = collectSignRequests(viewModel)
    advanceUntilIdle()

    viewModel.remove(FEED_A)
    advanceUntilIdle()

    assertEquals(FeedSubscriptionsMessage.ReadOnly, viewModel.uiState.value.message)
    assertTrue(requests.isEmpty())
  }

  @Test
  fun `removing a feed asks the signer to sign the new list`() = runTest {
    coEvery { prepareChange.remove(FEED_A) } returns readyToSign()
    val viewModel = createViewModel()
    val requests = collectSignRequests(viewModel)
    advanceUntilIdle()

    viewModel.remove(FEED_A)
    advanceUntilIdle()

    coVerify(exactly = 1) { prepareChange.remove(FEED_A) }
    assertEquals(listOf(signIntent), requests)
  }

  @Test
  fun `removing a feed that is already gone reloads the list`() = runTest {
    coEvery { prepareChange.remove(FEED_A) } returns
        Result.success(FeedSubscriptionChange.NotSubscribed(FEED_A))
    val viewModel = createViewModel()
    advanceUntilIdle()
    coEvery { loadSubscriptions() } returns Result.success(emptyList())

    viewModel.remove(FEED_A)
    advanceUntilIdle()

    assertEquals(
        FeedSubscriptionsUiState.Content.Loaded(emptyList()), viewModel.uiState.value.content)
    assertFalse(viewModel.uiState.value.isWorking)
  }

  @Test
  fun `a change is ignored while another one is in progress`() = runTest {
    coEvery { prepareChange.add(any()) } returns readyToSign()
    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.add(INPUT)
    advanceUntilIdle()
    viewModel.remove(FEED_A)
    advanceUntilIdle()

    coVerify(exactly = 0) { prepareChange.remove(any()) }
  }

  @Test
  fun `a signed list is published and the list is reloaded`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns readyToSign()
    givenSignerReturns(SIGNATURE)
    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.add(INPUT)
    advanceUntilIdle()
    coEvery { loadSubscriptions() } returns Result.success(listOf(FEED_A, FEED_B))

    viewModel.processSignedEvent(Activity.RESULT_OK, Intent())
    advanceUntilIdle()

    coVerify(exactly = 1) { publishList(event.toSignedJson(SIGNATURE)) }
    val state = viewModel.uiState.value
    assertEquals(FeedSubscriptionsUiState.Content.Loaded(listOf(FEED_A, FEED_B)), state.content)
    assertFalse(state.isWorking)
    assertTrue(state.hasPublished, "the timeline has to learn that the list changed")
    assertNull(state.message)
  }

  @Test
  fun `a rejected signature is reported and nothing is published`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns readyToSign()
    every { nip55SignerClient.handleSignEventResponse(any(), any()) } returns
        Result.failure(Nip55Error.UserRejected)
    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.add(INPUT)
    advanceUntilIdle()

    viewModel.processSignedEvent(Activity.RESULT_CANCELED, null)
    advanceUntilIdle()

    coVerify(exactly = 0) { publishList(any()) }
    assertEquals(FeedSubscriptionsMessage.SigningCancelled, viewModel.uiState.value.message)
    assertFalse(viewModel.uiState.value.isWorking)
    assertFalse(viewModel.uiState.value.hasPublished)
  }

  @Test
  fun `relays that refused the list are reported without blocking`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns readyToSign()
    givenSignerReturns(SIGNATURE)
    coEvery { publishList(any()) } returns
        Result.success(PublishResult("id", listOf(RELAY), listOf("wss://down.example" to "error")))
    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.add(INPUT)
    advanceUntilIdle()

    viewModel.processSignedEvent(Activity.RESULT_OK, Intent())
    advanceUntilIdle()

    assertEquals(FeedSubscriptionsMessage.PartiallyPublished, viewModel.uiState.value.message)
    assertTrue(viewModel.uiState.value.hasPublished)
  }

  @Test
  fun `a failed publish is reported`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns readyToSign()
    givenSignerReturns(SIGNATURE)
    coEvery { publishList(any()) } returns Result.failure(IOException("no relay accepted"))
    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.add(INPUT)
    advanceUntilIdle()

    viewModel.processSignedEvent(Activity.RESULT_OK, Intent())
    advanceUntilIdle()

    assertEquals(FeedSubscriptionsMessage.PublishFailed, viewModel.uiState.value.message)
    assertFalse(viewModel.uiState.value.isWorking)
    assertFalse(viewModel.uiState.value.hasPublished)
  }

  @Test
  fun `a signature without the pending event cannot be published`() = runTest {
    givenSignerReturns(SIGNATURE)
    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.processSignedEvent(Activity.RESULT_OK, Intent())
    advanceUntilIdle()

    coVerify(exactly = 0) { publishList(any()) }
    assertEquals(FeedSubscriptionsMessage.SignedEventBuildFailed, viewModel.uiState.value.message)
  }

  @Test
  fun `the pending list survives the ViewModel being recreated`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns readyToSign()
    givenSignerReturns(SIGNATURE)
    createViewModel().add(INPUT)
    advanceUntilIdle()
    val restoredHandle =
        SavedStateHandle(savedStateHandle.keys().associateWith { savedStateHandle.get<Any>(it) })

    val restored = createViewModel(restoredHandle)
    restored.processSignedEvent(Activity.RESULT_OK, Intent())
    advanceUntilIdle()

    coVerify(exactly = 1) { publishList(event.toSignedJson(SIGNATURE)) }
    assertTrue(restored.uiState.value.hasPublished)
  }

  @Test
  fun `dismissing the message clears it`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns
        Result.success(FeedSubscriptionChange.AlreadySubscribed(FEED_A))
    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.add(INPUT)
    advanceUntilIdle()

    viewModel.dismissMessage()

    assertNull(viewModel.uiState.value.message)
  }

  @Test
  fun `reload loads the list again`() = runTest {
    coEvery { loadSubscriptions() } returns
        Result.failure(
            FeedSubscriptionListUnavailableException(FeedSubscriptionLoadResult.Reason.UNREACHABLE))
    val viewModel = createViewModel()
    advanceUntilIdle()
    coEvery { loadSubscriptions() } returns Result.success(listOf(FEED_B))

    viewModel.reload()

    assertEquals(
        FeedSubscriptionsUiState.Content.Loading,
        viewModel.uiState.value.content,
        "retrying after an error shows progress instead of the old error")
    advanceUntilIdle()

    assertEquals(
        FeedSubscriptionsUiState.Content.Loaded(listOf(FEED_B)), viewModel.uiState.value.content)
  }

  @Test
  fun `an older load does not overwrite a newer one`() = runTest {
    val stale = CompletableDeferred<Result<List<String>>>()
    coEvery { loadSubscriptions() } coAnswers { stale.await() }
    val viewModel = createViewModel()
    runCurrent()
    coEvery { loadSubscriptions() } returns Result.success(listOf(FEED_B))

    viewModel.reload()
    advanceUntilIdle()
    stale.complete(Result.success(listOf(FEED_A)))
    advanceUntilIdle()

    assertEquals(
        FeedSubscriptionsUiState.Content.Loaded(listOf(FEED_B)), viewModel.uiState.value.content)
  }

  @Test
  fun `the sign request carries the intent built by the signer client`() = runTest {
    coEvery { prepareChange.add(INPUT) } returns readyToSign()
    val viewModel = createViewModel()
    val requests = collectSignRequests(viewModel)
    advanceUntilIdle()

    viewModel.add(INPUT)
    advanceUntilIdle()

    assertSame(signIntent, requests.single())
  }

  private companion object {
    const val HEX_PUBKEY = "fffbfffffffffffffffffffffffffffffffffffffffffffffffffffffffff3ff"
    const val INPUT = "blog.example"
    const val FEED_A = "https://a.example.com/feed.xml"
    const val FEED_B = "https://b.example.com/feed.xml"
    const val RELAY = "wss://relay.example"
    const val SIGNATURE =
        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef" +
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
  }
}
