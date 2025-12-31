package io.github.omochice.pinosu.domain.model

/**
 * User entity (aggregate root)
 *
 * Domain model representing the login state of a Nostr user. Only holds the public key (pubkey) and
 * never stores the private key.
 *
 * Task 2.1: Domain model implementation Requirements: 1.4, 6.1
 *
 * @property pubkey Nostr public key (Bech32-encoded format, starts with npub1)
 * @throws IllegalArgumentException if pubkey is in an invalid format
 */
data class User(val pubkey: String) {
  init {
    require(pubkey.isValidNostrPubkey()) {
      "Invalid Nostr pubkey format: must be Bech32-encoded (npub1...)"
    }
  }
}

/**
 * Extension function for validating Nostr public key
 *
 * Validates that the public key is in Bech32-encoded format (starts with npub1).
 *
 * @return true if the public key format is valid, false otherwise
 */
private fun String.isValidNostrPubkey(): Boolean {
  return this.startsWith("npub1")
}
