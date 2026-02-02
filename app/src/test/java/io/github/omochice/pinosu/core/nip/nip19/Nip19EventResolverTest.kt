package io.github.omochice.pinosu.core.nip.nip19

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test class for Nip19EventResolver
 *
 * Test scenarios:
 * 1. Extracts single nevent hex ID from content
 * 2. Extracts multiple nevent hex IDs from content
 * 3. Returns empty list when content has no nevent references
 * 4. Ignores invalid nevent bech32 strings
 */
class Nip19EventResolverTest {

  private val resolver = Nip19EventResolver()

  @Test
  fun `extractEventIds returns hex ID for single nevent reference`() {
    val content =
        "Check this out: nostr:nevent1qqs2qfg5f9jr5dwd6eyqnlsm85s68u9pnggvgprn0vq4hthkw32sssprpmhxue69uhhyetvv9ujumt0wd68ytnsw43z7qghwaehxw309aex2mrp0yhxummnw3ezucnpdejz7qpqylm6evwdnp8qdmrfnpnfhq7kzjjjxagat72tav8h76c2k6kaxnqxlzmyg"

    val ids = resolver.extractEventIds(content)

    assertEquals(1, ids.size)
  }

  @Test
  fun `extractEventIds returns multiple hex IDs for multiple nevent references`() {
    val content =
        "nostr:nevent1qqs2qfg5f9jr5dwd6eyqnlsm85s68u9pnggvgprn0vq4hthkw32sssprpmhxue69uhhyetvv9ujumt0wd68ytnsw43z7qghwaehxw309aex2mrp0yhxummnw3ezucnpdejz7qpqylm6evwdnp8qdmrfnpnfhq7kzjjjxagat72tav8h76c2k6kaxnqxlzmyg and nostr:nevent1qqs2qfg5f9jr5dwd6eyqnlsm85s68u9pnggvgprn0vq4hthkw32sssprpmhxue69uhhyetvv9ujumt0wd68ytnsw43z7qghwaehxw309aex2mrp0yhxummnw3ezucnpdejz7qpqylm6evwdnp8qdmrfnpnfhq7kzjjjxagat72tav8h76c2k6kaxnqxlzmyg"

    val ids = resolver.extractEventIds(content)

    assertEquals(2, ids.size)
  }

  @Test
  fun `extractEventIds returns empty list when no nevent references exist`() {
    val content = "Just a plain text comment with no references"

    val ids = resolver.extractEventIds(content)

    assertTrue(ids.isEmpty())
  }

  @Test
  fun `extractEventIds ignores malformed nostr URIs`() {
    val content = "nostr:invalid123 and nostr:npub1abc"

    val ids = resolver.extractEventIds(content)

    assertTrue(ids.isEmpty())
  }
}
