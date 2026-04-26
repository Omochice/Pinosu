package io.github.omochice.pinosu.core.nip.nipb0

/** NIP-B0 protocol constants for bookmark lists */
object NipB0 {
  const val KIND_BOOKMARK_LIST = 39701

  /**
   * Create a NIP-33 address string for a bookmark list
   *
   * @param pubkey Hex-encoded public key of the author
   * @param identifier The bookmark identifier (d-tag value)
   * @return Address string in the format "kind:pubkey:identifier"
   */
  fun createAddress(pubkey: String, identifier: String): String =
      "$KIND_BOOKMARK_LIST:$pubkey:$identifier"

  /** Tag keys used in kind 39701 bookmark events */
  object Tag {
    const val IDENTIFIER = "d"
    const val CATEGORY = "t"
    const val URL = "r"
    const val TITLE = "title"
  }
}
