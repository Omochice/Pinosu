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

  private val validNevent =
      "nostr:nevent1qy2hwumn8ghj7un9d3shjtnyv9kh2uewd9hj7qgwwaehxw309ahx7uewd3hkctcpr9mhxue69uhhy" +
          "etvv9ujuumwdae8gtnnda3kjctv9uq36amnwvaz7tmjv4kxz7fwvd5xjcmpvahhqmr9vfejucm0d5hsz9mhwden5" +
          "te0wfjkccte9ec8y6tdv9kzumn9wshsz8thwden5te0dehhxarj9ekh2arfdeuhwctvd3jhgtnrdakj7qg3waehxw" +
          "309ucngvpwvcmh5tnfduhszythwden5te0dehhxarj9emkjmn99uq3jamnwvaz7tmhv4kxxmmdv5hxummnw3ezuam" +
          "fdejj7qpqvsup5xk3e2quedxjvn2gjppc0lqny5dmnr2ypc9tftwmdxta0yjqrd6n50"
  private val validNeventHex = "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924"

  @Test
  fun `extractEventIds returns hex ID for single nevent reference`() {
    val content = "Check this out: $validNevent"

    val ids = resolver.extractEventIds(content)

    assertEquals(1, ids.size)
    assertEquals(validNeventHex, ids[0])
  }

  @Test
  fun `extractEventIds returns multiple hex IDs for multiple nevent references`() {
    val content = "$validNevent and $validNevent"

    val ids = resolver.extractEventIds(content)

    assertEquals(2, ids.size)
    assertEquals(validNeventHex, ids[0])
    assertEquals(validNeventHex, ids[1])
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
