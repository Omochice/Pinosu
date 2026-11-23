package io.github.omochice.pinosu.core.nip.nip78

/** NIP-78 protocol constants for arbitrary custom app data */
object Nip78 {
  const val KIND_APP_DATA = 30078

  /** Tag keys used in kind 30078 app data events */
  object Tag {
    const val IDENTIFIER = "d"
  }
}
