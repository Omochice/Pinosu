package io.github.omochice.pinosu.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [Pubkey] value object
 *
 * Uses Robolectric because Pubkey.hex depends on quartz library which needs Android runtime.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PubkeyTest {

  @Test
  fun `parse returns Pubkey for valid npub`() {
    val pubkey = Pubkey.parse(TEST_VALID_NPUB)

    assertNotNull(pubkey, "Should parse valid npub")
    assertEquals(TEST_VALID_NPUB, pubkey.npub, "Should preserve npub value")
  }

  @Test
  fun `parse returns null for hex pubkey`() {
    val hexPubkey = "a".repeat(64)

    val result = Pubkey.parse(hexPubkey)

    assertNull(result, "Should return null for hex format pubkey")
  }

  @Test
  fun `parse returns null for empty string`() {
    val result = Pubkey.parse("")

    assertNull(result, "Should return null for empty string")
  }

  @Test
  fun `parse returns null for nsec`() {
    val nsec = "nsec1" + "a".repeat(59)

    val result = Pubkey.parse(nsec)

    assertNull(result, "Should return null for nsec format")
  }

  @Test
  fun `parse returns null for nprofile`() {
    val nprofile = "nprofile1" + "a".repeat(54)

    val result = Pubkey.parse(nprofile)

    assertNull(result, "Should return null for nprofile format")
  }

  @Test
  fun `parse returns null for uppercase NPUB1`() {
    val uppercaseNpub = "NPUB1" + "a".repeat(59)

    val result = Pubkey.parse(uppercaseNpub)

    assertNull(result, "Should return null for uppercase NPUB1")
  }

  @Test
  fun `hex returns hex string for valid npub`() {
    val pubkey = Pubkey.parse(TEST_VALID_NPUB)

    assertNotNull(pubkey, "Should parse valid npub")
    assertEquals(TEST_VALID_HEX, pubkey.hex, "Should return correct hex")
  }

  @Test
  fun `hex returns null for npub with invalid checksum`() {
    val invalidChecksumNpub = "npub1" + "a".repeat(58)

    val pubkey = Pubkey.parse(invalidChecksumNpub)

    assertNotNull(pubkey, "Should parse npub format")
    assertNull(pubkey.hex, "Should return null for invalid checksum")
  }

  @Test
  fun `equality works correctly for same npub`() {
    val pubkey1 = Pubkey.parse(TEST_VALID_NPUB)
    val pubkey2 = Pubkey.parse(TEST_VALID_NPUB)

    assertNotNull(pubkey1, "Should parse first npub")
    assertNotNull(pubkey2, "Should parse second npub")
    assertEquals(pubkey1, pubkey2, "Same npub should be equal")
    assertEquals(pubkey1.hashCode(), pubkey2.hashCode(), "Same npub should have same hashCode")
  }

  @Test
  fun `equality works correctly for different npub`() {
    val pubkey1 = Pubkey.parse(TEST_VALID_NPUB)
    val pubkey2 = Pubkey.parse("npub1" + "q".repeat(58))

    assertNotNull(pubkey1, "Should parse first npub")
    assertNotNull(pubkey2, "Should parse second npub")
    assertEquals(false, pubkey1 == pubkey2)
  }

  @Test
  fun `isValidFormat returns true for valid npub`() {
    val result = Pubkey.isValidFormat(TEST_VALID_NPUB)

    assertEquals(true, result, "Should return true for valid npub")
  }

  @Test
  fun `isValidFormat returns false for hex pubkey`() {
    val hexPubkey = "a".repeat(64)

    val result = Pubkey.isValidFormat(hexPubkey)

    assertEquals(false, result, "Should return false for hex format pubkey")
  }

  @Test
  fun `isValidFormat returns false for empty string`() {
    val result = Pubkey.isValidFormat("")

    assertEquals(false, result, "Should return false for empty string")
  }

  @Test
  fun `isValidFormat returns false for nsec`() {
    val nsec = "nsec1" + "a".repeat(59)

    val result = Pubkey.isValidFormat(nsec)

    assertEquals(false, result, "Should return false for nsec format")
  }

  @Test
  fun `isValidFormat returns false for nprofile`() {
    val nprofile = "nprofile1" + "a".repeat(54)

    val result = Pubkey.isValidFormat(nprofile)

    assertEquals(false, result, "Should return false for nprofile format")
  }

  @Test
  fun `isValidFormat returns false for uppercase NPUB1`() {
    val uppercaseNpub = "NPUB1" + "a".repeat(59)

    val result = Pubkey.isValidFormat(uppercaseNpub)

    assertEquals(false, result, "Should return false for uppercase NPUB1")
  }

  companion object {
    /**
     * Valid test npub for testing. This is fiatjaf's npub (well-known Nostr developer) which is a
     * real Bech32-encoded public key that passes checksum validation.
     */
    const val TEST_VALID_NPUB = "npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m"
    const val TEST_VALID_HEX = "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"
  }
}
