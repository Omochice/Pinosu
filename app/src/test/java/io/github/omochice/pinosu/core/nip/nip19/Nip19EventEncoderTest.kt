package io.github.omochice.pinosu.core.nip.nip19

import com.vitorpamplona.quartz.nip19Bech32.Nip19Parser
import com.vitorpamplona.quartz.nip19Bech32.entities.NAddress
import io.github.omochice.pinosu.core.nip.nipb0.NipB0
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Nip19EventEncoderTest {

  private val encoder = Nip19EventEncoder()

  @Test
  fun `encodeNAddr returns string starting with nostr naddr1`() {
    val pubkey = "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924"
    val dTag = "bookmark-id-001"

    val result = encoder.encodeNAddr(kind = NipB0.KIND_BOOKMARK_LIST, pubkey = pubkey, dTag = dTag)

    assertTrue(
        "Encoded naddr should start with nostr:naddr1 but was '$result'",
        result.startsWith("nostr:naddr1"))
  }

  @Test
  fun `encodeNAddr output decodes back to original kind pubkey and dTag`() {
    val pubkey = "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924"
    val dTag = "bookmark-id-001"

    val encoded = encoder.encodeNAddr(kind = NipB0.KIND_BOOKMARK_LIST, pubkey = pubkey, dTag = dTag)

    val decoded = Nip19Parser.uriToRoute(encoded)?.entity as? NAddress
    assertNotNull("Encoded naddr should be decodable", decoded)
    assertEquals(NipB0.KIND_BOOKMARK_LIST, decoded?.kind)
    assertEquals(pubkey, decoded?.author)
    assertEquals(dTag, decoded?.dTag)
  }
}
