package io.github.omochice.pinosu.feature.feed.data.parser

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeedDateParserTest {

  @Test
  fun `RFC 822 date with a numeric offset is converted to UTC`() {
    assertEquals(
        Instant.parse("2024-01-01T18:04:05Z"),
        FeedDateParser.parse("Tue, 02 Jan 2024 03:04:05 +0900"))
  }

  @Test
  fun `RFC 822 date in GMT`() {
    assertEquals(
        Instant.parse("2024-01-02T03:04:05Z"),
        FeedDateParser.parse("Tue, 02 Jan 2024 03:04:05 GMT"))
  }

  @Test
  fun `RFC 822 date in UT`() {
    assertEquals(
        Instant.parse("2024-01-02T03:04:05Z"), FeedDateParser.parse("Tue, 02 Jan 2024 03:04:05 UT"))
  }

  @Test
  fun `RFC 822 date in JST is nine hours ahead of UTC`() {
    assertEquals(
        Instant.parse("2024-01-01T18:04:05Z"),
        FeedDateParser.parse("Tue, 02 Jan 2024 03:04:05 JST"))
  }

  @Test
  fun `RFC 822 date in EST is five hours behind UTC`() {
    assertEquals(
        Instant.parse("2024-01-02T08:04:05Z"),
        FeedDateParser.parse("Tue, 02 Jan 2024 03:04:05 EST"))
  }

  @Test
  fun `RFC 822 date in PDT is seven hours behind UTC`() {
    assertEquals(
        Instant.parse("2024-07-02T10:04:05Z"),
        FeedDateParser.parse("Tue, 02 Jul 2024 03:04:05 PDT"))
  }

  @Test
  fun `RFC 822 date without a weekday`() {
    assertEquals(
        Instant.parse("2024-01-02T03:04:05Z"), FeedDateParser.parse("02 Jan 2024 03:04:05 +0000"))
  }

  @Test
  fun `RFC 822 date whose weekday does not match the date is still read`() {
    assertEquals(
        Instant.parse("2024-01-02T03:04:05Z"),
        FeedDateParser.parse("Fri, 02 Jan 2024 03:04:05 GMT"))
  }

  @Test
  fun `RFC 822 date with a two-digit year`() {
    assertEquals(
        Instant.parse("2024-01-02T03:04:05Z"), FeedDateParser.parse("Tue, 02 Jan 24 03:04:05 GMT"))
  }

  @Test
  fun `RFC 822 date with a one-digit day and no seconds`() {
    assertEquals(
        Instant.parse("2024-01-02T03:04:00Z"), FeedDateParser.parse("Tue, 2 Jan 2024 03:04 GMT"))
  }

  @Test
  fun `RFC 822 date with surrounding whitespace`() {
    assertEquals(
        Instant.parse("2024-01-02T03:04:05Z"),
        FeedDateParser.parse("\n  Tue, 02 Jan 2024 03:04:05 GMT\n"))
  }

  @Test
  fun `ISO 8601 date with an offset`() {
    assertEquals(
        Instant.parse("2024-01-01T18:04:05Z"), FeedDateParser.parse("2024-01-02T03:04:05+09:00"))
  }

  @Test
  fun `ISO 8601 date in UTC with Z`() {
    assertEquals(
        Instant.parse("2024-01-02T03:04:05Z"), FeedDateParser.parse("2024-01-02T03:04:05Z"))
  }

  @Test
  fun `ISO 8601 date with fractional seconds`() {
    assertEquals(
        Instant.parse("2024-01-02T03:04:05.123Z"), FeedDateParser.parse("2024-01-02T03:04:05.123Z"))
  }

  @Test
  fun `ISO 8601 date without a time is the start of that day in UTC`() {
    assertEquals(Instant.parse("2024-01-02T00:00:00Z"), FeedDateParser.parse("2024-01-02"))
  }

  @Test
  fun `text that is not a date gives null`() {
    assertNull(FeedDateParser.parse("yesterday"))
  }

  @Test
  fun `blank text gives null`() {
    assertNull(FeedDateParser.parse("  "))
  }
}
