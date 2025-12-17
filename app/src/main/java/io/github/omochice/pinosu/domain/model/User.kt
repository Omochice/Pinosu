package io.github.omochice.pinosu.domain.model

/**
 * User entity (aggregate root)
 *
 * Domain model representing the login state of a Nostr user. Only holds the public key (pubkey) and
 * never stores the private key.
 *
 * Task 2.1: Domain model implementation Requirements: 1.4, 6.1
 *
 * @property pubkey Nostr public key (64 hexadecimal characters)
 * @throws IllegalArgumentException if pubkey is in an invalid format
 */
data class User(val pubkey: String) {
  init {
    require(pubkey.isValidNostrPubkey()) {
      "Invalid Nostr pubkey format: must be 64 hex characters"
    }
  }
}

/**
 * Extension function for validating Nostr public key
 *
 * Validates that the public key is 64 hexadecimal characters (0-9, a-f).
 *
 * @return true if the public key format is valid, false otherwise
 */
private fun String.isValidNostrPubkey(): Boolean {
  return this.matches(Regex("^[0-9a-f]{64}$"))
}
