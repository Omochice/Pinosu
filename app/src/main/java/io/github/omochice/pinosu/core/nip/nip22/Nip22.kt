package io.github.omochice.pinosu.core.nip.nip22

/** NIP-22 protocol constants for comments */
object Nip22 {
  const val KIND_COMMENT = 1111

  /** Tag keys used in kind 1111 comment events */
  object Tag {
    const val ADDRESS = "a"
    const val EVENT = "e"
    const val KIND = "k"
    const val PUBKEY = "p"

    const val ADDRESS_ROOT = "A"
    const val EVENT_ROOT = "E"
    const val KIND_ROOT = "K"
    const val PUBKEY_ROOT = "P"
  }
}
