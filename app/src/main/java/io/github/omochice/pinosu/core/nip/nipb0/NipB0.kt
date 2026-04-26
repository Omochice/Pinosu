package io.github.omochice.pinosu.core.nip.nipb0

/** NIP-B0 protocol constants for bookmark lists */
object NipB0 {
  const val KIND_BOOKMARK_LIST = 39701

  /** Tag keys used in kind 39701 bookmark events */
  object Tag {
    const val IDENTIFIER = "d"
    const val CATEGORY = "t"
    const val URL = "r"
    const val TITLE = "title"
  }
}
