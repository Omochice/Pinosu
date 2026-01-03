package io.github.omochice.pinosu.domain.util

/**
 * Bech32 encoding/decoding utility for Nostr public keys
 *
 * Converts between npub (Bech32) format and hex format
 */
object Bech32 {

  private const val CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"

  /**
   * Decode npub to hex format
   *
   * @param npub Bech32-encoded public key (starts with npub1)
   * @return Hex-encoded public key or null if decoding fails
   */
  fun npubToHex(npub: String): String? {
    if (!npub.startsWith("npub1")) {
      return null
    }

    return try {
      val data = npub.substring(5) // Remove "npub1" prefix
      val decoded = decode(data)
      if (decoded == null || decoded.size != 32) {
        return null
      }
      decoded.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Decode Bech32 data part to byte array
   *
   * @param data Bech32 data string
   * @return Decoded byte array or null if decoding fails
   */
  private fun decode(data: String): ByteArray? {
    val values = data.map { char -> CHARSET.indexOf(char) }
    if (values.any { it == -1 }) {
      return null
    }

    // Convert from 5-bit to 8-bit
    val bits = mutableListOf<Int>()
    var accumulator = 0
    var bitsCount = 0

    for (value in values.dropLast(6)) { // Remove checksum (last 6 characters)
      accumulator = (accumulator shl 5) or value
      bitsCount += 5
      while (bitsCount >= 8) {
        bitsCount -= 8
        bits.add((accumulator shr bitsCount) and 0xFF)
        accumulator = accumulator and ((1 shl bitsCount) - 1)
      }
    }

    return bits.map { it.toByte() }.toByteArray()
  }
}
