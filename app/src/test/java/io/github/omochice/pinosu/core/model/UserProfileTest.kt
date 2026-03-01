package io.github.omochice.pinosu.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Test class for UserProfile domain model */
class UserProfileTest {

  @Test
  fun `holds all fields when fully populated`() {
    val profile =
        UserProfile(
            pubkey = "abcd1234",
            name = "Alice",
            picture = "https://example.com/avatar.png",
            about = "Nostr user")

    assertEquals("abcd1234", profile.pubkey)
    assertEquals("Alice", profile.name)
    assertEquals("https://example.com/avatar.png", profile.picture)
    assertEquals("Nostr user", profile.about)
  }

  @Test
  fun `optional fields default to null`() {
    val profile = UserProfile(pubkey = "abcd1234")

    assertEquals("abcd1234", profile.pubkey)
    assertNull(profile.name)
    assertNull(profile.picture)
    assertNull(profile.about)
  }
}
