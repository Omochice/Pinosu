package io.github.omochice.pinosu.core.nip.nip19

import com.vitorpamplona.quartz.nip19Bech32.Nip19Parser
import com.vitorpamplona.quartz.nip19Bech32.entities.NEvent
import javax.inject.Inject

/**
 * Resolves NIP-19 nevent references from text content
 *
 * Finds `nostr:nevent1...` patterns in a string and decodes each into a hex event ID using Quartz's
 * [Nip19Parser].
 */
class Nip19EventResolver @Inject constructor() {

  /**
   * Extract hex event IDs from all `nostr:nevent1...` references in [content]
   *
   * @param content Text that may contain nostr:nevent1... URIs
   * @return List of hex-encoded event IDs (may contain duplicates if the same nevent appears
   *   multiple times)
   */
  fun extractEventIds(content: String): List<String> {
    return NEVENT_PATTERN.findAll(content)
        .mapNotNull { match ->
          val uri = match.value
          val route = Nip19Parser.uriToRoute(uri)
          (route?.entity as? NEvent)?.hex
        }
        .toList()
  }

  companion object {
    // The data part uses the bech32 character set (which excludes 1, b, i and o) rather than
    // [a-z0-9]. Those four characters can never appear in a valid nevent, so the match ends at
    // directly adjacent words instead of swallowing them, which would corrupt the encoded value
    // and make an otherwise valid quoted event fail to decode.
    private val NEVENT_PATTERN = Regex("""nostr:nevent1[ac-hj-np-z02-9]+""")
  }
}
