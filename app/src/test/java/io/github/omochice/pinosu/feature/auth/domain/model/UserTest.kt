package io.github.omochice.pinosu.feature.auth.domain.model

import org.junit.Assert.*
import org.junit.Test

class UserTest {

  /** Valid pubkey（Bech32 format）can create User test */
  @Test
  fun `create User with valid pubkey`() {
    val validPubkey = "npub1" + "a".repeat(59)
    val user = User(validPubkey)

    assertEquals(validPubkey, user.pubkey)
  }

  /** Another valid pubkey（Bech32 format with numbers and lowercase）can create User test */
  @Test
  fun `create User with valid hex pubkey containing numbers`() {
    val validPubkey = "npub1" + "0123456789abcdef".repeat(3) + "0123456789a"
    val user = User(validPubkey)

    assertTrue(user.pubkey.startsWith("npub1"))
    assertEquals(validPubkey, user.pubkey)
  }

  /** Test IllegalArgumentException thrown for pubkey not starting with npub1 */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with pubkey too short throws exception`() {
    val shortPubkey = "npub" + "a".repeat(59) // starts with npub, not npub1
    User(shortPubkey)
  }

  /** Test IllegalArgumentException thrown for hex format (old format) pubkey */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with pubkey too long throws exception`() {
    val longPubkey = "a".repeat(64) // hex format without npub1 prefix
    User(longPubkey)
  }

  /** Test IllegalArgumentException thrown for pubkey not starting with npub1 (uppercase NPUB1) */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with uppercase characters throws exception`() {
    val invalidPubkey = "NPUB1" + "A".repeat(59) // uppercase NPUB1
    User(invalidPubkey)
  }

  /** Test IllegalArgumentException thrown for pubkey starting with nsec (secret key format) */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with non-hex characters throws exception`() {
    val invalidPubkey = "nsec1" + "g".repeat(59) // nsec1 is secret key format
    User(invalidPubkey)
  }

  /** Test IllegalArgumentException thrown for empty pubkey */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with empty pubkey throws exception`() {
    User("")
  }

  /** Test IllegalArgumentException thrown for pubkey starting with nprofile1 (profile format) */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with spaces throws exception`() {
    val invalidPubkey = "nprofile1" + "a".repeat(54) // nprofile1 is profile format
    User(invalidPubkey)
  }

  /** User equality（data class property）test */
  @Test
  fun `User equality works correctly`() {
    val pubkey = "npub1" + "a".repeat(59)
    val user1 = User(pubkey)
    val user2 = User(pubkey)

    assertEquals(user1, user2)
    assertEquals(user1.hashCode(), user2.hashCode())
  }

  /** Test User with different pubkey is not equal */
  @Test
  fun `Users with different pubkeys are not equal`() {
    val user1 = User("npub1" + "a".repeat(59))
    val user2 = User("npub1" + "b".repeat(59))

    assertNotEquals(user1, user2)
  }
}
