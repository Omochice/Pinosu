package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class PublishFeedSubscriptionListUseCaseTest {

  private lateinit var subscriptionRepository: FeedSubscriptionRepository
  private lateinit var useCase: PublishFeedSubscriptionListUseCase

  @BeforeTest
  fun setup() {
    subscriptionRepository = mockk()
    useCase = PublishFeedSubscriptionListUseCaseImpl(subscriptionRepository)
  }

  @Test
  fun `returns the relays that accepted and rejected the list`() = runTest {
    val published =
        PublishResult(
            eventId = "abc",
            successfulRelays = listOf("wss://ok.example.com"),
            failedRelays = listOf("wss://ng.example.com" to "blocked"))
    coEvery { subscriptionRepository.publishSubscriptionList(SIGNED) } returns
        Result.success(published)

    val result = useCase(SIGNED)

    assertEquals(published, result.getOrNull())
  }

  @Test
  fun `returns the publishing failure`() = runTest {
    val error = IOException("no relay")
    coEvery { subscriptionRepository.publishSubscriptionList(SIGNED) } returns Result.failure(error)

    val result = useCase(SIGNED)

    assertSame(error, result.exceptionOrNull())
  }

  private companion object {
    const val SIGNED = """{"id":"abc","sig":"def"}"""
  }
}
