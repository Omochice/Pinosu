package io.github.omochice.pinosu.core.url

import kotlin.test.Test
import kotlin.test.assertEquals

class TrackingQueryStripperTest {

  @Test
  fun `removes utm_source`() {
    assertEquals(
        "https://example.com/page",
        stripTrackingQueryParameters("https://example.com/page?utm_source=newsletter"))
  }

  @Test
  fun `removes utm_campaign among other utm_ keys`() {
    assertEquals(
        "https://example.com/",
        stripTrackingQueryParameters("https://example.com/?utm_campaign=spring&utm_medium=email"))
  }

  @Test
  fun `removes uppercase UTM_SOURCE`() {
    assertEquals(
        "https://example.com/page",
        stripTrackingQueryParameters("https://example.com/page?UTM_SOURCE=x"))
  }

  @Test
  fun `removes fbclid`() {
    assertEquals(
        "https://example.com/page",
        stripTrackingQueryParameters("https://example.com/page?fbclid=abc123"))
  }

  @Test
  fun `removes FBCLID case-insensitively`() {
    assertEquals(
        "https://example.com/page",
        stripTrackingQueryParameters("https://example.com/page?FBCLID=abc123"))
  }

  @Test
  fun `does not inspect values`() {
    assertEquals(
        "https://example.com/page?q=utm_source",
        stripTrackingQueryParameters("https://example.com/page?q=utm_source"))
  }

  @Test
  fun `keeps content query while removing tracking key`() {
    assertEquals(
        "https://youtube.com/watch?v=abc",
        stripTrackingQueryParameters("https://youtube.com/watch?v=abc&utm_source=x"))
  }

  @Test
  fun `keeps percent-encoded value byte-identical`() {
    assertEquals(
        "https://example.com/search?q=%E3%81%82",
        stripTrackingQueryParameters("https://example.com/search?q=%E3%81%82&gclid=1"))
  }

  @Test
  fun `keeps encoded ampersand in value byte-identical`() {
    assertEquals(
        "https://example.com/?x=a%26b=1",
        stripTrackingQueryParameters("https://example.com/?x=a%26b=1&fbclid=z"))
  }

  @Test
  fun `preserves order of surviving segments`() {
    assertEquals(
        "https://example.com/?b=2&a=1&c=3",
        stripTrackingQueryParameters("https://example.com/?b=2&utm_term=t&a=1&msclkid=m&c=3"))
  }

  @Test
  fun `preserves fragment when query is fully removed`() {
    assertEquals(
        "https://example.com/page#frag",
        stripTrackingQueryParameters("https://example.com/page?utm_source=x#frag"))
  }

  @Test
  fun `preserves fragment alongside surviving query`() {
    assertEquals(
        "https://example.com/page?id=1#sec",
        stripTrackingQueryParameters("https://example.com/page?id=1&yclid=y#sec"))
  }

  @Test
  fun `drops question mark when all segments are removed`() {
    assertEquals(
        "https://example.com/page",
        stripTrackingQueryParameters("https://example.com/page?utm_source=a&gclid=b"))
  }

  @Test
  fun `keeps empty segments as-is`() {
    assertEquals(
        "https://example.com/?a=1&&b=2",
        stripTrackingQueryParameters("https://example.com/?a=1&&b=2"))
  }

  @Test
  fun `keeps key-only flag segment`() {
    assertEquals(
        "https://example.com/?flag", stripTrackingQueryParameters("https://example.com/?flag"))
  }

  @Test
  fun `removes key-only tracking segment`() {
    assertEquals(
        "https://example.com/page", stripTrackingQueryParameters("https://example.com/page?fbclid"))
  }

  @Test
  fun `handles scheme-less input`() {
    assertEquals(
        "example.com/page?v=1", stripTrackingQueryParameters("example.com/page?v=1&utm_source=x"))
  }

  @Test
  fun `returns url without query unchanged`() {
    assertEquals(
        "https://example.com/page", stripTrackingQueryParameters("https://example.com/page"))
  }

  @Test
  fun `keeps bare trailing question mark`() {
    assertEquals("https://example.com/?", stripTrackingQueryParameters("https://example.com/?"))
  }

  @Test
  fun `does not remove utmx which lacks the underscore`() {
    assertEquals(
        "https://example.com/?utmx=1", stripTrackingQueryParameters("https://example.com/?utmx=1"))
  }

  @Test
  fun `does not remove ttclid`() {
    assertEquals(
        "https://example.com/?ttclid=1",
        stripTrackingQueryParameters("https://example.com/?ttclid=1"))
  }

  @Test
  fun `treats question mark inside fragment as fragment`() {
    assertEquals(
        "https://example.com/page#frag?utm_source=x",
        stripTrackingQueryParameters("https://example.com/page#frag?utm_source=x"))
  }
}
