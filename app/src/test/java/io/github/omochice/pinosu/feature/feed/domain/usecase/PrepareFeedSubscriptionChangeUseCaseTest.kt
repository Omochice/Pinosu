package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip78.Nip78
import io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.feed.domain.model.Feed
import io.github.omochice.pinosu.feature.feed.domain.model.FeedCandidate
import io.github.omochice.pinosu.feature.feed.domain.model.FeedResolution
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionChange
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.model.error.NoFeedFoundException
import io.github.omochice.pinosu.feature.feed.domain.model.error.ReadOnlyLoginException
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedDiscoveryRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class PrepareFeedSubscriptionChangeUseCaseTest {

  private lateinit var subscriptionRepository: FeedSubscriptionRepository
  private lateinit var discoveryRepository: FeedDiscoveryRepository
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var authRepository: AuthRepository
  private lateinit var useCase: PrepareFeedSubscriptionChangeUseCase

  @BeforeTest
  fun setup() {
    subscriptionRepository = mockk()
    discoveryRepository = mockk()
    getLoginStateUseCase = mockk()
    authRepository = mockk()
    useCase =
        PrepareFeedSubscriptionChangeUseCaseImpl(
            subscriptionRepository, discoveryRepository, getLoginStateUseCase, authRepository)
    coEvery { getLoginStateUseCase() } returns User(Pubkey.parse(VALID_NPUB)!!)
    coEvery { authRepository.getLoginMode() } returns LoginMode.Nip55Signer
    every { subscriptionRepository.createSubscriptionListEvent(any(), any(), any()) } returns EVENT
  }

  @Test
  fun `add fails when the user is not logged in`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    val result = useCase.add(INPUT)

    assertIs<IllegalStateException>(result.exceptionOrNull())
    coVerify(exactly = 0) { discoveryRepository.resolve(any()) }
  }

  @Test
  fun `remove fails when the user is not logged in`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    val result = useCase.remove(FEED_A)

    assertIs<IllegalStateException>(result.exceptionOrNull())
    coVerify(exactly = 0) { subscriptionRepository.loadSubscriptionList(any()) }
  }

  @Test
  fun `add is refused for a read-only login before anything is fetched`() = runTest {
    coEvery { authRepository.getLoginMode() } returns LoginMode.ReadOnly

    val result = useCase.add(INPUT)

    assertIs<ReadOnlyLoginException>(result.exceptionOrNull())
    coVerify(exactly = 0) { discoveryRepository.resolve(any()) }
    coVerify(exactly = 0) { subscriptionRepository.loadSubscriptionList(any()) }
    verify(exactly = 0) { subscriptionRepository.createSubscriptionListEvent(any(), any(), any()) }
  }

  @Test
  fun `remove is refused for a read-only login before anything is fetched`() = runTest {
    coEvery { authRepository.getLoginMode() } returns LoginMode.ReadOnly

    val result = useCase.remove(FEED_A)

    assertIs<ReadOnlyLoginException>(result.exceptionOrNull())
    coVerify(exactly = 0) { subscriptionRepository.loadSubscriptionList(any()) }
    verify(exactly = 0) { subscriptionRepository.createSubscriptionListEvent(any(), any(), any()) }
  }

  @Test
  fun `add appends the resolved feed to the loaded list`() = runTest {
    val current = FeedSubscriptionList(listOf(FEED_A), createdAt = 1L)
    givenLoaded(FeedSubscriptionLoadResult.Found(current))
    givenResolvedDirect(FEED_B)

    val result = useCase.add(INPUT)

    assertEquals(
        FeedSubscriptionChange.ReadyToSign(EVENT, listOf(FEED_A, FEED_B), isFirstList = false),
        result.getOrNull())
    verify(exactly = 1) {
      subscriptionRepository.createSubscriptionListEvent(
          HEX_PUBKEY, listOf(FEED_A, FEED_B), current)
    }
  }

  @Test
  fun `add resolves the input, then loads the list, then builds the event`() = runTest {
    givenLoaded(FeedSubscriptionLoadResult.Found(FeedSubscriptionList(listOf(FEED_A), 1L)))
    givenResolvedDirect(FEED_B)

    useCase.add(INPUT)

    coVerifyOrder {
      discoveryRepository.resolve(INPUT)
      subscriptionRepository.loadSubscriptionList(HEX_PUBKEY)
      subscriptionRepository.createSubscriptionListEvent(HEX_PUBKEY, any(), any())
    }
  }

  @Test
  fun `add of a feed already in the list builds no event`() = runTest {
    givenLoaded(FeedSubscriptionLoadResult.Found(FeedSubscriptionList(listOf(FEED_A), 1L)))
    givenResolvedDirect(FEED_A)

    val result = useCase.add(INPUT)

    assertEquals(FeedSubscriptionChange.AlreadySubscribed(FEED_A), result.getOrNull())
    verify(exactly = 0) { subscriptionRepository.createSubscriptionListEvent(any(), any(), any()) }
  }

  @Test
  fun `add of a page with several feeds returns the candidates without loading the list`() =
      runTest {
        val candidates = listOf(FeedCandidate(FEED_A, "Posts"), FeedCandidate(FEED_B, "Comments"))
        coEvery { discoveryRepository.resolve(INPUT) } returns
            Result.success(FeedResolution.Candidates(candidates))

        val result = useCase.add(INPUT)

        assertEquals(FeedSubscriptionChange.ChooseCandidate(candidates), result.getOrNull())
        coVerify(exactly = 0) { subscriptionRepository.loadSubscriptionList(any()) }
        verify(exactly = 0) {
          subscriptionRepository.createSubscriptionListEvent(any(), any(), any())
        }
      }

  @Test
  fun `add fails with the discovery error when no feed is found`() = runTest {
    val error = NoFeedFoundException("https://example.com/")
    coEvery { discoveryRepository.resolve(INPUT) } returns Result.failure(error)

    val result = useCase.add(INPUT)

    assertSame(error, result.exceptionOrNull())
    coVerify(exactly = 0) { subscriptionRepository.loadSubscriptionList(any()) }
    verify(exactly = 0) { subscriptionRepository.createSubscriptionListEvent(any(), any(), any()) }
  }

  @Test
  fun `add to an absent list builds the first list and flags it`() = runTest {
    givenLoaded(FeedSubscriptionLoadResult.Absent)
    givenResolvedDirect(FEED_A)

    val result = useCase.add(INPUT)

    assertEquals(
        FeedSubscriptionChange.ReadyToSign(EVENT, listOf(FEED_A), isFirstList = true),
        result.getOrNull())
    verify(exactly = 1) {
      subscriptionRepository.createSubscriptionListEvent(HEX_PUBKEY, listOf(FEED_A), null)
    }
  }

  @Test
  fun `add to an unavailable list fails with the reason and builds no event`() = runTest {
    givenLoaded(
        FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.UNREACHABLE))
    givenResolvedDirect(FEED_A)

    val result = useCase.add(INPUT)

    val error = assertIs<FeedSubscriptionListUnavailableException>(result.exceptionOrNull())
    assertEquals(FeedSubscriptionLoadResult.Reason.UNREACHABLE, error.reason)
    verify(exactly = 0) { subscriptionRepository.createSubscriptionListEvent(any(), any(), any()) }
  }

  @Test
  fun `every change loads the list again`() = runTest {
    val first = FeedSubscriptionList(listOf(FEED_A), createdAt = 1L)
    val second = FeedSubscriptionList(listOf(FEED_A, FEED_B), createdAt = 2L)
    coEvery { subscriptionRepository.loadSubscriptionList(HEX_PUBKEY) } returnsMany
        listOf(FeedSubscriptionLoadResult.Found(first), FeedSubscriptionLoadResult.Found(second))
    givenResolvedDirect(FEED_C)

    useCase.add(INPUT)
    val result = useCase.add(INPUT)

    assertEquals(
        FeedSubscriptionChange.ReadyToSign(
            EVENT, listOf(FEED_A, FEED_B, FEED_C), isFirstList = false),
        result.getOrNull())
    coVerify(exactly = 2) { subscriptionRepository.loadSubscriptionList(HEX_PUBKEY) }
    verify(exactly = 1) {
      subscriptionRepository.createSubscriptionListEvent(
          HEX_PUBKEY, listOf(FEED_A, FEED_B, FEED_C), second)
    }
  }

  @Test
  fun `remove drops the feed from the loaded list`() = runTest {
    val current = FeedSubscriptionList(listOf(FEED_A, FEED_B, FEED_C), createdAt = 1L)
    givenLoaded(FeedSubscriptionLoadResult.Found(current))

    val result = useCase.remove(FEED_B)

    assertEquals(
        FeedSubscriptionChange.ReadyToSign(EVENT, listOf(FEED_A, FEED_C), isFirstList = false),
        result.getOrNull())
    verify(exactly = 1) {
      subscriptionRepository.createSubscriptionListEvent(
          HEX_PUBKEY, listOf(FEED_A, FEED_C), current)
    }
  }

  @Test
  fun `remove of the last feed builds an empty list`() = runTest {
    val current = FeedSubscriptionList(listOf(FEED_A), createdAt = 1L)
    givenLoaded(FeedSubscriptionLoadResult.Found(current))

    val result = useCase.remove(FEED_A)

    assertEquals(
        FeedSubscriptionChange.ReadyToSign(EVENT, emptyList(), isFirstList = false),
        result.getOrNull())
    verify(exactly = 1) {
      subscriptionRepository.createSubscriptionListEvent(HEX_PUBKEY, emptyList(), current)
    }
  }

  @Test
  fun `remove of a feed not in the list builds no event`() = runTest {
    givenLoaded(FeedSubscriptionLoadResult.Found(FeedSubscriptionList(listOf(FEED_A), 1L)))

    val result = useCase.remove(FEED_B)

    assertEquals(FeedSubscriptionChange.NotSubscribed(FEED_B), result.getOrNull())
    verify(exactly = 0) { subscriptionRepository.createSubscriptionListEvent(any(), any(), any()) }
  }

  @Test
  fun `remove from an absent list builds no event`() = runTest {
    givenLoaded(FeedSubscriptionLoadResult.Absent)

    val result = useCase.remove(FEED_A)

    assertEquals(FeedSubscriptionChange.NotSubscribed(FEED_A), result.getOrNull())
    verify(exactly = 0) { subscriptionRepository.createSubscriptionListEvent(any(), any(), any()) }
  }

  @Test
  fun `remove from an unavailable list fails with the reason and builds no event`() = runTest {
    givenLoaded(
        FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.NO_USER_RELAYS))

    val result = useCase.remove(FEED_A)

    val error = assertIs<FeedSubscriptionListUnavailableException>(result.exceptionOrNull())
    assertEquals(FeedSubscriptionLoadResult.Reason.NO_USER_RELAYS, error.reason)
    verify(exactly = 0) { subscriptionRepository.createSubscriptionListEvent(any(), any(), any()) }
  }

  @Test
  fun `remove loads the list on every call`() = runTest {
    givenLoaded(FeedSubscriptionLoadResult.Found(FeedSubscriptionList(listOf(FEED_A), 1L)))

    useCase.remove(FEED_A)
    useCase.remove(FEED_A)

    coVerify(exactly = 2) { subscriptionRepository.loadSubscriptionList(HEX_PUBKEY) }
  }

  private fun givenLoaded(result: FeedSubscriptionLoadResult) {
    coEvery { subscriptionRepository.loadSubscriptionList(HEX_PUBKEY) } returns result
  }

  private fun givenResolvedDirect(feedUrl: String) {
    coEvery { discoveryRepository.resolve(INPUT) } returns
        Result.success(
            FeedResolution.Direct(
                feedUrl, Feed(url = feedUrl, title = "", siteUrl = null, items = emptyList())))
  }

  private companion object {
    const val VALID_NPUB = "npub1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq4hl3lg"
    const val HEX_PUBKEY = "fffbfffffffffffffffffffffffffffffffffffffffffffffffffffffffff3ff"
    const val INPUT = "example.com"
    const val FEED_A = "https://example.com/a.xml"
    const val FEED_B = "https://example.com/b.xml"
    const val FEED_C = "https://example.com/c.xml"
    val EVENT =
        UnsignedNostrEvent(
            pubkey = HEX_PUBKEY,
            createdAt = 1L,
            kind = Nip78.KIND_APP_DATA,
            tags = emptyList(),
            content = "")
  }
}
