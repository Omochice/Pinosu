package io.github.omochice.pinosu.feature.auth.domain.model

import io.github.omochice.pinosu.core.model.Pubkey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthEventTest {

  /** Test that UserLoggedIn event can be created */
  @Test
  fun `create UserLoggedIn event`() {
    val user = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    val event = AuthEvent.UserLoggedIn(user)

    assertTrue(event is AuthEvent)
    assertTrue(event is AuthEvent.UserLoggedIn)
    assertEquals(user, event.user)
  }

  /** Test that UserLoggedOut event can be created */
  @Test
  fun `create UserLoggedOut event`() {
    val event = AuthEvent.UserLoggedOut

    assertTrue(event is AuthEvent)
    assertTrue(event is AuthEvent.UserLoggedOut)
  }

  /** Test UserLoggedIn event equality */
  @Test
  fun `UserLoggedIn events with same user are equal`() {
    val user = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    val event1 = AuthEvent.UserLoggedIn(user)
    val event2 = AuthEvent.UserLoggedIn(user)

    assertEquals(event1, event2)
  }

  /** Test UserLoggedIn events with different users are not equal */
  @Test
  fun `UserLoggedIn events with different users are not equal`() {
    val user1 = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    val user2 = User(Pubkey.parse("npub1" + "b".repeat(59))!!)
    val event1 = AuthEvent.UserLoggedIn(user1)
    val event2 = AuthEvent.UserLoggedIn(user2)

    assertNotEquals(event1, event2)
  }

  /** Test UserLoggedOut event singleton nature */
  @Test
  fun `UserLoggedOut is singleton`() {
    val event1 = AuthEvent.UserLoggedOut
    val event2 = AuthEvent.UserLoggedOut

    assertSame(event1, event2)
  }

  /** Test AuthEvent sealed class properties（when expression exhaustiveness） */
  @Test
  fun `AuthEvent sealed class allows exhaustive when`() {
    val user = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    val loggedInEvent: AuthEvent = AuthEvent.UserLoggedIn(user)
    val loggedOutEvent: AuthEvent = AuthEvent.UserLoggedOut

    // Verify when expression is exhaustive（compile-time check）
    val loggedInResult =
        when (loggedInEvent) {
          is AuthEvent.UserLoggedIn -> "logged_in"
          is AuthEvent.UserLoggedOut -> "logged_out"
        }

    val loggedOutResult =
        when (loggedOutEvent) {
          is AuthEvent.UserLoggedIn -> "logged_in"
          is AuthEvent.UserLoggedOut -> "logged_out"
        }

    assertEquals("logged_in", loggedInResult)
    assertEquals("logged_out", loggedOutResult)
  }
}
