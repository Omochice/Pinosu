package io.github.omochice.pinosu.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
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

    assertNotNull("Should parse valid npub", pubkey)
    assertEquals("Should preserve npub value", TEST_VALID_NPUB, pubkey?.npub)
  }

  @Test
  fun `parse returns null for hex pubkey`() {
    val hexPubkey = "a".repeat(64)

    val result = Pubkey.parse(hexPubkey)

    assertNull("Should return null for hex format pubkey", result)
  }

  @Test
  fun `parse returns null for empty string`() {
    val result = Pubkey.parse("")

    assertNull("Should return null for empty string", result)
  }

  @Test
  fun `parse returns null for nsec`() {
    val nsec = "nsec1" + "a".repeat(59)

    val result = Pubkey.parse(nsec)

    assertNull("Should return null for nsec format", result)
  }

  @Test
  fun `parse returns null for nprofile`() {
    val nprofile = "nprofile1" + "a".repeat(54)

    val result = Pubkey.parse(nprofile)

    assertNull("Should return null for nprofile format", result)
  }

  @Test
  fun `parse returns null for uppercase NPUB1`() {
    val uppercaseNpub = "NPUB1" + "a".repeat(59)

    val result = Pubkey.parse(uppercaseNpub)

    assertNull("Should return null for uppercase NPUB1", result)
  }

  @Test
  fun `hex returns hex string for valid npub`() {
    val pubkey = Pubkey.parse(TEST_VALID_NPUB)

    assertNotNull("Should parse valid npub", pubkey)
    assertEquals("Should return correct hex", TEST_VALID_HEX, pubkey?.hex)
  }

  @Test
  fun `hex returns null for npub with invalid checksum`() {
    val invalidChecksumNpub = "npub1" + "a".repeat(58)

    val pubkey = Pubkey.parse(invalidChecksumNpub)

    assertNotNull("Should parse npub format", pubkey)
    assertNull("Should return null for invalid checksum", pubkey?.hex)
  }

  @Test
  fun `equality works correctly for same npub`() {
    val pubkey1 = Pubkey.parse(TEST_VALID_NPUB)
    val pubkey2 = Pubkey.parse(TEST_VALID_NPUB)

    assertEquals("Same npub should be equal", pubkey1, pubkey2)
    assertEquals("Same npub should have same hashCode", pubkey1?.hashCode(), pubkey2?.hashCode())
  }

  @Test
  fun `equality works correctly for different npub`() {
    val pubkey1 = Pubkey.parse(TEST_VALID_NPUB)
    val pubkey2 = Pubkey.parse("npub1" + "q".repeat(58))

    assertNotNull("Should parse first npub", pubkey1)
    assertNotNull("Should parse second npub", pubkey2)
    assertEquals(false, pubkey1 == pubkey2)
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
