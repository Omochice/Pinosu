package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class LoadFeedSubscriptionsUseCaseTest {

  private lateinit var subscriptionRepository: FeedSubscriptionRepository
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var useCase: LoadFeedSubscriptionsUseCase

  @BeforeTest
  fun setup() {
    subscriptionRepository = mockk()
    getLoginStateUseCase = mockk()
    useCase = LoadFeedSubscriptionsUseCaseImpl(subscriptionRepository, getLoginStateUseCase)
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
  fun `a found list gives its feed URLs in stored order`() = runTest {
    coEvery { subscriptionRepository.loadSubscriptionList(any()) } returns
        FeedSubscriptionLoadResult.Found(FeedSubscriptionList(feeds(FEED_B, FEED_A), 1L))

    val result = useCase()

    assertEquals(feeds(FEED_B, FEED_A), result.getOrNull())
  }

  @Test
  fun `an absent list gives no feeds`() = runTest {
    coEvery { subscriptionRepository.loadSubscriptionList(any()) } returns
        FeedSubscriptionLoadResult.Absent

    val result = useCase()

    assertEquals(emptyList(), result.getOrNull())
  }

  @Test
  fun `an unavailable list fails with its reason`() = runTest {
    coEvery { subscriptionRepository.loadSubscriptionList(any()) } returns
        FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.ACCESS_DENIED)

    val result = useCase()

    val error = assertIs<FeedSubscriptionListUnavailableException>(result.exceptionOrNull())
    assertEquals(FeedSubscriptionLoadResult.Reason.ACCESS_DENIED, error.reason)
  }

  private fun feeds(vararg feedUrls: String): List<FeedSubscription> =
      feedUrls.map(::FeedSubscription)

  private companion object {
    const val VALID_NPUB = "npub1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq4hl3lg"
    const val HEX_PUBKEY = "fffbfffffffffffffffffffffffffffffffffffffffffffffffffffffffff3ff"
    const val FEED_A = "https://a.example.com/feed.xml"
    const val FEED_B = "https://b.example.com/feed.xml"
  }
}
