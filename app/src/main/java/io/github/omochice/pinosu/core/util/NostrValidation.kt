package io.github.omochice.pinosu.core.util

/**
 * Extension function for validating Nostr public key
 *
 * Validates that the public key is in Bech32-encoded format (starts with npub1).
 *
 * @return true if the public key format is valid, false otherwise
 */
fun String.isValidNostrPubkey(): Boolean = this.startsWith("npub1")
