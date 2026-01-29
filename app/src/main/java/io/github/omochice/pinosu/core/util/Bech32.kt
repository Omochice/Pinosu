package io.github.omochice.pinosu.core.util

import com.vitorpamplona.quartz.nip19Bech32.decodePublicKeyAsHexOrNull

/**
 * Bech32 encoding/decoding utility for Nostr public keys
 *
 * Delegates to quartz library for proper Bech32 handling with checksum validation
 */
object Bech32 {
  /**
   * Convert npub (Bech32-encoded public key) to hex format
   *
   * @param npub Bech32-encoded public key (starts with npub1)
   * @return 64-character hex string, or null if conversion fails or checksum invalid
   */
  fun npubToHex(npub: String): String? {
    return decodePublicKeyAsHexOrNull(npub)
  }
}
