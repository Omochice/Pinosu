package io.github.omochice.pinosu.feature.auth.domain.model

import kotlinx.serialization.Serializable

/**
 * Login mode representing how the user authenticated.
 *
 * Determines user capabilities: [Nip55Signer] allows full read/write, [ReadOnly] restricts to
 * browse-only.
 */
@Serializable
sealed interface LoginMode {

  /** Whether this login mode restricts the user to read-only access */
  val isReadOnly: Boolean

  /** Full login via NIP-55 external signer (e.g., Amber) */
  @Serializable
  data object Nip55Signer : LoginMode {
    override val isReadOnly: Boolean = false
  }

  /** Read-only login via direct npub public key entry */
  @Serializable
  data object ReadOnly : LoginMode {
    override val isReadOnly: Boolean = true
  }
}
