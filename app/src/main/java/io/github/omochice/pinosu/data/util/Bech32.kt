package io.github.omochice.pinosu.data.util

/**
 * Bech32 encoding/decoding utility for Nostr public keys
 *
 * Converts between npub (Bech32) format and hex format
 */
object Bech32 {

  private const val CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"

  fun npubToHex(npub: String): String? {
    if (!npub.startsWith("npub1")) {
      return null
    }

    return try {
      val data = npub.substring(5)
      val decoded = decode(data)
      if (decoded == null || decoded.size != 32) {
        return null
      }
      decoded.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
      null
    }
  }

  private fun decode(data: String): ByteArray? {
    val values = data.map { char -> CHARSET.indexOf(char) }
    if (values.any { it == -1 }) {
      return null
    }

    val bits = mutableListOf<Int>()
    var accumulator = 0
    var bitsCount = 0

    for (value in values.dropLast(6)) {
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
