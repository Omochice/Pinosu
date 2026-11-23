package io.github.omochice.pinosu.feature.feed.data.codec

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip78.Nip78
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeedSubscriptionListCodecTest {

  private val codec = FeedSubscriptionListCodec()

  @Test
  fun `encode produces a kind 30078 event with the Pinosu d tag and one url tag per feed`() {
    val event = encode(listOf(FEED_A, FEED_B))

    assertEquals(Nip78.KIND_APP_DATA, event.kind)
    assertEquals(AUTHOR, event.pubkey)
    assertEquals("", event.content)
    assertEquals(
        listOf(
            listOf("d", "pinosu/rss-subscriptions"),
            listOf("url", FEED_A),
            listOf("url", FEED_B),
        ),
        event.tags)
  }

  @Test
  fun `encode keeps the first occurrence of duplicate urls in order`() {
    val event = encode(listOf(FEED_B, FEED_A, FEED_B))

    assertEquals(listOf(FEED_B, FEED_A), urlTagValues(event))
  }

  @Test
  fun `encode drops blank urls`() {
    val event = encode(listOf("", FEED_A, "   "))

    assertEquals(listOf(FEED_A), urlTagValues(event))
  }

  @Test
  fun `encode of an empty list has the d tag but no url tags`() {
    val event = encode(emptyList())

    assertEquals(listOf(listOf("d", "pinosu/rss-subscriptions")), event.tags)
  }

  @Test
  fun `encode appends the client tag only when requested`() {
    val withTag = encode(listOf(FEED_A), includeClientTag = true)
    val withoutTag = encode(listOf(FEED_A), includeClientTag = false)

    assertEquals(listOf("client", "Pinosu"), withTag.tags.last())
    assertEquals(listOf(FEED_A), urlTagValues(withoutTag))
    assertEquals(2, withoutTag.tags.size)
  }

  @Test
  fun `encode without a previous list uses now as created_at`() {
    val event = encode(listOf(FEED_A), now = NOW, previousCreatedAt = null)

    assertEquals(NOW, event.createdAt)
  }

  @Test
  fun `encode uses now when it is later than the previous list`() {
    val event = encode(listOf(FEED_A), now = NOW, previousCreatedAt = NOW - 10)

    assertEquals(NOW, event.createdAt)
  }

  @Test
  fun `encode uses one second after the previous list when now is not later`() {
    val sameSecond = encode(listOf(FEED_A), now = NOW, previousCreatedAt = NOW)
    val clockBehind = encode(listOf(FEED_A), now = NOW, previousCreatedAt = NOW + 100)

    assertEquals(NOW + 1, sameSecond.createdAt)
    assertEquals(NOW + 101, clockBehind.createdAt)
  }

  @Test
  fun `decode of an encoded event returns the same urls and created_at`() {
    val encoded = encode(listOf(FEED_A, FEED_B), now = NOW)

    val decoded = codec.decode(signed(encoded), AUTHOR)

    assertEquals(
        FeedSubscriptionList(listOf(FeedSubscription(FEED_A), FeedSubscription(FEED_B)), NOW),
        decoded)
  }

  @Test
  fun `decode collects url tags in order without duplicates or blanks`() {
    val event =
        listEvent(
            tags =
                listOf(
                    listOf("d", "pinosu/rss-subscriptions"),
                    listOf("url", FEED_B),
                    listOf("url", ""),
                    listOf("url", FEED_A),
                    listOf("url", FEED_B),
                ))

    assertEquals(listOf(FEED_B, FEED_A), decodedUrls(event))
  }

  @Test
  fun `decode ignores tags other than url`() {
    val event =
        listEvent(
            tags =
                listOf(
                    listOf("d", "pinosu/rss-subscriptions"),
                    listOf("r", "https://ignored.example.com/"),
                    listOf("url"),
                    listOf("client", "Pinosu"),
                    listOf("url", FEED_A),
                ))

    assertEquals(listOf(FEED_A), decodedUrls(event))
  }

  @Test
  fun `decode returns null for an event by another author`() {
    assertNull(codec.decode(listEvent(pubkey = OTHER_AUTHOR), AUTHOR))
  }

  @Test
  fun `decode returns null for an event of another kind`() {
    assertNull(codec.decode(listEvent(kind = Nip78.KIND_APP_DATA + 1), AUTHOR))
  }

  @Test
  fun `decode returns null for an app data event with another d value`() {
    val event = listEvent(tags = listOf(listOf("d", "other-app/settings"), listOf("url", FEED_A)))

    assertNull(codec.decode(event, AUTHOR))
  }

  @Test
  fun `decode returns null for an event without a d tag`() {
    assertNull(codec.decode(listEvent(tags = listOf(listOf("url", FEED_A))), AUTHOR))
  }

  private fun encode(
      feedUrls: List<String>,
      now: Long = NOW,
      previousCreatedAt: Long? = null,
      includeClientTag: Boolean = false,
  ): UnsignedNostrEvent =
      codec.encode(
          AUTHOR, feedUrls.map(::FeedSubscription), now, previousCreatedAt, includeClientTag)

  private fun decodedUrls(event: NostrEvent): List<String>? =
      codec.decode(event, AUTHOR)?.feeds?.map { it.url }

  private fun urlTagValues(event: UnsignedNostrEvent): List<String> =
      event.tags.filter { it.first() == "url" }.map { it[1] }

  private fun signed(event: UnsignedNostrEvent): NostrEvent =
      NostrEvent(
          id = "id",
          pubkey = event.pubkey,
          createdAt = event.createdAt,
          kind = event.kind,
          tags = event.tags,
          content = event.content,
          sig = "sig")

  private fun listEvent(
      pubkey: String = AUTHOR,
      kind: Int = Nip78.KIND_APP_DATA,
      tags: List<List<String>> =
          listOf(listOf("d", "pinosu/rss-subscriptions"), listOf("url", FEED_A)),
  ): NostrEvent =
      NostrEvent(
          id = "id",
          pubkey = pubkey,
          createdAt = NOW,
          kind = kind,
          tags = tags,
          content = "",
          sig = "sig")

  private companion object {
    val AUTHOR = "a".repeat(64)
    val OTHER_AUTHOR = "b".repeat(64)
    const val NOW = 1_700_000_000L
    const val FEED_A = "https://a.example.com/feed.xml"
    const val FEED_B = "https://b.example.com/atom.xml"
  }
}
