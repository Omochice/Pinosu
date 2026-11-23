package io.github.omochice.pinosu.core.relay

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RelayTerminationTest {

  @Test
  fun `Closed with auth-required prefix is access denied`() {
    assertTrue(RelayTermination.Closed("auth-required: please authenticate").isAccessDenied)
  }

  @Test
  fun `Closed with restricted prefix is access denied`() {
    assertTrue(RelayTermination.Closed("restricted: members only").isAccessDenied)
  }

  @Test
  fun `Closed with a prefix and no description is access denied`() {
    assertTrue(RelayTermination.Closed("restricted:").isAccessDenied)
  }

  @Test
  fun `Closed with another machine-readable prefix is not access denied`() {
    assertFalse(RelayTermination.Closed("error: shutting down").isAccessDenied)
  }

  @Test
  fun `Closed whose prefix only starts with restricted is not access denied`() {
    assertFalse(RelayTermination.Closed("restricted-ish: unknown").isAccessDenied)
  }

  @Test
  fun `Closed without a colon is not access denied`() {
    assertFalse(RelayTermination.Closed("restricted").isAccessDenied)
  }

  @Test
  fun `Closed with an empty message is not access denied`() {
    assertFalse(RelayTermination.Closed("").isAccessDenied)
  }
}
