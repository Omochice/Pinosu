package io.github.omochice.pinosu.feature.auth.domain.model

import io.github.omochice.pinosu.core.model.Pubkey

/**
 * User entity (aggregate root)
 *
 * Domain model representing the login state of a Nostr user. Only holds the public key (pubkey) and
 * never stores the private key.
 *
 * @property pubkey Nostr public key as Pubkey value object
 */
data class User(val pubkey: Pubkey)
