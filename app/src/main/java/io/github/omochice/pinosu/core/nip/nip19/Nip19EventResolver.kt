package io.github.omochice.pinosu.core.nip.nip19

import com.vitorpamplona.quartz.nip19Bech32.Nip19Parser
import com.vitorpamplona.quartz.nip19Bech32.entities.NEvent

/**
 * Resolves NIP-19 nevent references from text content
 *
 * Finds `nostr:nevent1...` patterns in a string and decodes each into a hex event ID using Quartz's
 * [Nip19Parser].
 */
class Nip19EventResolver {

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
    private val NEVENT_PATTERN = Regex("""nostr:nevent1[a-z0-9]+""")
  }
}
