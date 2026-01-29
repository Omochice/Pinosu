package io.github.omochice.pinosu.core.model

import com.vitorpamplona.quartz.nip19Bech32.decodePublicKeyAsHexOrNull

/**
 * Nostr public key value object
 *
 * Encapsulates a Bech32-encoded public key (npub1...) and provides conversion to hex format. This
 * value object ensures type safety at domain boundaries and centralizes npub-related operations.
 *
 * @property npub Bech32-encoded public key (starts with npub1)
 */
@JvmInline
value class Pubkey private constructor(val npub: String) {

  /**
   * Convert npub to hex format
   *
   * @return 64-character hex string, or null if conversion fails or checksum is invalid
   */
  val hex: String?
    get() = decodePublicKeyAsHexOrNull(npub)

  companion object {
    private const val NPUB_PREFIX = "npub1"

    /**
     * Parse a string as Pubkey
     *
     * @param value String to parse (expected to be npub1...)
     * @return Pubkey if the string starts with "npub1", null otherwise
     */
    fun parse(value: String): Pubkey? = if (isValidFormat(value)) Pubkey(value) else null

    /**
     * Check if a string has valid npub format
     *
     * @param value String to check
     * @return true if the string starts with "npub1", false otherwise
     */
    fun isValidFormat(value: String): Boolean = value.startsWith(NPUB_PREFIX)
  }
}
