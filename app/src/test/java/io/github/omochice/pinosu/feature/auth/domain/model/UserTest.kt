package io.github.omochice.pinosu.feature.auth.domain.model

import io.github.omochice.pinosu.core.model.Pubkey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTest {

  @Test
  fun `create User with valid Pubkey`() {
    val validNpub = "npub1" + "a".repeat(59)
    val pubkey = Pubkey.parse(validNpub)!!
    val user = User(pubkey)

    assertEquals(validNpub, user.pubkey.npub)
  }

  @Test
  fun `User pubkey provides access to npub`() {
    val validNpub = "npub1" + "0123456789abcdef".repeat(3) + "0123456789a"
    val pubkey = Pubkey.parse(validNpub)!!
    val user = User(pubkey)

    assertTrue(user.pubkey.npub.startsWith("npub1"))
    assertEquals(validNpub, user.pubkey.npub)
  }

  @Test
  fun `Pubkey parse returns null for invalid formats`() {
    val invalidFormats =
        listOf(
            "npub" + "a".repeat(59),
            "a".repeat(64),
            "NPUB1" + "A".repeat(59),
            "nsec1" + "g".repeat(59),
            "",
            "nprofile1" + "a".repeat(54))

    invalidFormats.forEach { invalid ->
      val result = Pubkey.parse(invalid)
      assertEquals("Should return null for: $invalid", null, result)
    }
  }

  @Test
  fun `User equality works correctly`() {
    val npub = "npub1" + "a".repeat(59)
    val user1 = User(Pubkey.parse(npub)!!)
    val user2 = User(Pubkey.parse(npub)!!)

    assertEquals(user1, user2)
    assertEquals(user1.hashCode(), user2.hashCode())
  }

  @Test
  fun `Users with different pubkeys are not equal`() {
    val user1 = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    val user2 = User(Pubkey.parse("npub1" + "b".repeat(59))!!)

    assertNotEquals(user1, user2)
  }

  @Test
  fun `User pubkey hex property delegates to Pubkey`() {
    val validNpub = "npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m"
    val expectedHex = "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"
    val pubkey = Pubkey.parse(validNpub)
    assertNotNull("Should parse valid npub", pubkey)
    val user = User(pubkey!!)

    assertEquals(expectedHex, user.pubkey.hex)
  }
}
