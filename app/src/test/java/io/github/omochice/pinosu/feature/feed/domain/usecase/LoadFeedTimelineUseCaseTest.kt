package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.feed.domain.model.Feed
import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.FeedTimeline
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class LoadFeedTimelineUseCaseTest {

  private lateinit var subscriptionRepository: FeedSubscriptionRepository
  private lateinit var feedRepository: FeedRepository
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var useCase: LoadFeedTimelineUseCase

  @BeforeTest
  fun setup() {
    subscriptionRepository = mockk()
    feedRepository = mockk()
    getLoginStateUseCase = mockk()
    useCase =
        LoadFeedTimelineUseCaseImpl(
            LoadFeedSubscriptionsUseCaseImpl(subscriptionRepository, getLoginStateUseCase),
            feedRepository)
    coEvery { getLoginStateUseCase() } returns User(Pubkey.parse(VALID_NPUB)!!)
  }

  @Test
  fun `fails when the user is not logged in`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    val result = useCase()

    assertIs<IllegalStateException>(result.exceptionOrNull())
    coVerify(exactly = 0) { subscriptionRepository.loadSubscriptionList(any()) }
  }

  @Test
  fun `loads the list of the logged-in user`() = runTest {
    coEvery { subscriptionRepository.loadSubscriptionList(HEX_PUBKEY) } returns
        FeedSubscriptionLoadResult.Absent

    useCase()

    coVerify(exactly = 1) { subscriptionRepository.loadSubscriptionList(HEX_PUBKEY) }
  }

  @Test
  fun `an absent list gives an empty timeline`() = runTest {
    coEvery { subscriptionRepository.loadSubscriptionList(any()) } returns
        FeedSubscriptionLoadResult.Absent

    val result = useCase()

    assertEquals(FeedTimeline(items = emptyList(), failedFeeds = emptyList()), result.getOrNull())
  }

  @Test
  fun `an empty list gives an empty timeline`() = runTest {
    givenSubscriptions()

    val result = useCase()

    assertEquals(FeedTimeline(items = emptyList(), failedFeeds = emptyList()), result.getOrNull())
  }

  @Test
  fun `an unavailable list fails with the reason`() = runTest {
    coEvery { subscriptionRepository.loadSubscriptionList(any()) } returns
        FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.ACCESS_DENIED)

    val result = useCase()

    val error = assertIs<FeedSubscriptionListUnavailableException>(result.exceptionOrNull())
    assertEquals(FeedSubscriptionLoadResult.Reason.ACCESS_DENIED, error.reason)
  }

  @Test
  fun `items of all feeds are merged newest first`() = runTest {
    givenSubscriptions(FEED_A, FEED_B)
    val a1 = item(FEED_A, "a1", "2026-01-01T00:00:00Z")
    val a2 = item(FEED_A, "a2", "2026-01-03T00:00:00Z")
    val b1 = item(FEED_B, "b1", "2026-01-02T00:00:00Z")
    val b2 = item(FEED_B, "b2", "2026-01-04T00:00:00Z")
    givenFeed(FEED_A, a1, a2)
    givenFeed(FEED_B, b1, b2)

    val result = useCase()

    assertEquals(listOf(b2, a2, b1, a1), result.getOrNull()?.items)
  }

  @Test
  fun `undated items follow every dated item in subscription and document order`() = runTest {
    givenSubscriptions(FEED_A, FEED_B)
    val u1 = item(FEED_A, "u1", null)
    val dated = item(FEED_A, "dated", "2026-01-01T00:00:00Z")
    val u2 = item(FEED_A, "u2", null)
    val u3 = item(FEED_B, "u3", null)
    val older = item(FEED_B, "older", "2025-01-01T00:00:00Z")
    givenFeed(FEED_A, u1, dated, u2)
    givenFeed(FEED_B, u3, older)

    val result = useCase()

    assertEquals(listOf(dated, older, u1, u2, u3), result.getOrNull()?.items)
  }

  @Test
  fun `items published at the same time keep subscription order`() = runTest {
    givenSubscriptions(FEED_A, FEED_B)
    val a = item(FEED_A, "a", "2026-01-01T00:00:00Z")
    val b = item(FEED_B, "b", "2026-01-01T00:00:00Z")
    givenFeed(FEED_A, a)
    givenFeed(FEED_B, b)

    val result = useCase()

    assertEquals(listOf(a, b), result.getOrNull()?.items)
  }

  @Test
  fun `a feed that fails to fetch is reported while the other feeds are shown`() = runTest {
    givenSubscriptions(FEED_A, FEED_B)
    val error = IOException("timeout")
    val b = item(FEED_B, "b", "2026-01-01T00:00:00Z")
    coEvery { feedRepository.fetchFeed(FEED_A) } returns Result.failure(error)
    givenFeed(FEED_B, b)

    val result = useCase()

    assertEquals(
        FeedTimeline(items = listOf(b), failedFeeds = listOf(FeedFetchFailure(FEED_A, error))),
        result.getOrNull())
  }

  @Test
  fun `the timeline succeeds with every failure when no feed can be fetched`() = runTest {
    givenSubscriptions(FEED_A, FEED_B)
    val errorA = IOException("a")
    val errorB = IOException("b")
    coEvery { feedRepository.fetchFeed(FEED_A) } returns Result.failure(errorA)
    coEvery { feedRepository.fetchFeed(FEED_B) } returns Result.failure(errorB)

    val result = useCase()

    assertEquals(
        FeedTimeline(
            items = emptyList(),
            failedFeeds =
                listOf(FeedFetchFailure(FEED_A, errorA), FeedFetchFailure(FEED_B, errorB))),
        result.getOrNull())
  }

  @Test
  fun `feeds are fetched in parallel up to the concurrency limit`() = runTest {
    val urls = (1..10).map { "https://example.com/$it.xml" }
    givenSubscriptions(*urls.toTypedArray())
    var inFlight = 0
    var maxInFlight = 0
    coEvery { feedRepository.fetchFeed(any()) } coAnswers
        {
          inFlight++
          maxInFlight = maxOf(maxInFlight, inFlight)
          delay(100)
          inFlight--
          Result.success(Feed(url = firstArg(), title = "", siteUrl = null, items = emptyList()))
        }

    useCase()

    assertEquals(LoadFeedTimelineUseCaseImpl.MAX_CONCURRENT_FETCHES, maxInFlight)
    coVerify(exactly = urls.size) { feedRepository.fetchFeed(any()) }
  }

  @Test
  fun `each item carries the category of the feed it came from`() = runTest {
    givenFeeds(FeedSubscription(FEED_A, "Tech"), FeedSubscription(FEED_B))
    givenFeed(FEED_A, item(FEED_A, "a", "2026-01-02T00:00:00Z"))
    givenFeed(FEED_B, item(FEED_B, "b", "2026-01-01T00:00:00Z"))

    val result = useCase()

    assertEquals(
        listOf("a" to "Tech", "b" to null),
        result.getOrNull()?.items?.map { it.id to it.feedCategory })
  }

  @Test
  fun `the category follows the subscription even when the feed names another url`() = runTest {
    givenFeeds(FeedSubscription(FEED_A, "Tech"))
    givenFeed(FEED_A, item("https://moved.example.com/feed.xml", "a", null))

    val result = useCase()

    assertEquals(listOf("Tech"), result.getOrNull()?.items?.map { it.feedCategory })
  }

  @Test
  fun `a feed that fails to fetch is reported with its category`() = runTest {
    val error = IOException("timeout")
    givenFeeds(FeedSubscription(FEED_A, "Tech"), FeedSubscription(FEED_B))
    coEvery { feedRepository.fetchFeed(FEED_A) } returns Result.failure(error)
    coEvery { feedRepository.fetchFeed(FEED_B) } returns Result.failure(error)

    val result = useCase()

    assertEquals(
        listOf(FeedFetchFailure(FEED_A, error, "Tech"), FeedFetchFailure(FEED_B, error, null)),
        result.getOrNull()?.failedFeeds)
  }

  @Test
  fun `the timeline names every category a subscribed feed is in once in subscription order`() =
      runTest {
        givenFeeds(
            FeedSubscription(FEED_A, "Tech"),
            FeedSubscription(FEED_B),
            FeedSubscription(FEED_C, "News"),
            FeedSubscription(FEED_D, "Tech"))
        coEvery { feedRepository.fetchFeed(any()) } returns Result.failure(IOException("down"))

        val result = useCase()

        assertEquals(listOf("Tech", "News"), result.getOrNull()?.categories)
      }

  @Test
  fun `an absent list has no categories`() = runTest {
    coEvery { subscriptionRepository.loadSubscriptionList(any()) } returns
        FeedSubscriptionLoadResult.Absent

    val result = useCase()

    assertEquals(emptyList(), result.getOrNull()?.categories)
  }

  private fun givenFeeds(vararg feeds: FeedSubscription) {
    coEvery { subscriptionRepository.loadSubscriptionList(any()) } returns
        FeedSubscriptionLoadResult.Found(FeedSubscriptionList(feeds.toList(), createdAt = 1L))
  }

  private fun givenSubscriptions(vararg feedUrls: String) {
    coEvery { subscriptionRepository.loadSubscriptionList(any()) } returns
        FeedSubscriptionLoadResult.Found(
            FeedSubscriptionList(feedUrls.map(::FeedSubscription), createdAt = 1L))
  }

  private fun givenFeed(feedUrl: String, vararg items: FeedItem) {
    coEvery { feedRepository.fetchFeed(feedUrl) } returns
        Result.success(Feed(url = feedUrl, title = "", siteUrl = null, items = items.toList()))
  }

  private fun item(feedUrl: String, id: String, publishedAt: String?) =
      FeedItem(
          id = id,
          feedUrl = feedUrl,
          feedTitle = "",
          title = id,
          link = null,
          publishedAt = publishedAt?.let(Instant::parse),
          summary = "")

  private companion object {
    const val VALID_NPUB = "npub1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq4hl3lg"
    const val HEX_PUBKEY = "fffbfffffffffffffffffffffffffffffffffffffffffffffffffffffffff3ff"
    const val FEED_A = "https://a.example.com/feed.xml"
    const val FEED_B = "https://b.example.com/feed.xml"
    const val FEED_C = "https://c.example.com/feed.xml"
    const val FEED_D = "https://d.example.com/feed.xml"
  }
}
