package io.github.omochice.pinosu.core.nip.nip19

import com.vitorpamplona.quartz.nip19Bech32.entities.NAddress
import com.vitorpamplona.quartz.nip19Bech32.entities.NEvent
import javax.inject.Inject

/**
 * Encodes Nostr events into NIP-19 `nostr:` URIs.
 *
 * @see Nip19EventResolver for the inverse operation.
 */
class Nip19EventEncoder @Inject constructor() {

  /**
   * Encode a parameterized replaceable event reference as a `nostr:naddr1...` URI.
   *
   * @param kind Nostr event kind (e.g. 39701 for NIP-B0 bookmarks).
   * @param pubkey Hex-encoded author public key.
   * @param dTag `d` tag identifier of the addressable event.
   * @return URI of the form `nostr:naddr1...`.
   */
  fun encodeNAddr(kind: Int, pubkey: String, dTag: String): String {
    val bech32 = NAddress.create(kind = kind, pubKeyHex = pubkey, dTag = dTag, relay = null)
    return "nostr:$bech32"
  }

  /**
   * Encode a regular event reference as a `nostr:nevent1...` URI.
   *
   * @param eventId Hex-encoded event ID.
   * @param pubkey Hex-encoded author public key.
   * @param kind Nostr event kind (e.g. 1111 for NIP-22 comments).
   * @return URI of the form `nostr:nevent1...`.
   */
  fun encodeNEvent(eventId: String, pubkey: String, kind: Int): String {
    val bech32 = NEvent.create(idHex = eventId, author = pubkey, kind = kind, relay = null)
    return "nostr:$bech32"
  }
}
