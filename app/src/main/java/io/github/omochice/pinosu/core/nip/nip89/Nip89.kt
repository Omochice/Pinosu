package io.github.omochice.pinosu.core.nip.nip89

/** NIP-89 protocol constants for client identification */
object Nip89 {
  const val CLIENT_NAME = "Pinosu"

  /** Build a client tag for identifying this app in published events */
  fun clientTag(): List<String> = listOf("client", CLIENT_NAME)
}
