package io.github.omochice.pinosu.domain.model

import org.junit.Assert.*import org.junit.test


/** UserLoggedIntest that User can be created */ @test
 fun `create UserLoggedIn event`() {
 val user = User("npub1" + "a".repeat(59))
 val event = AuthEvent.UserLoggedIn(user)

 assertTrue(event is AuthEvent)
 assertTrue(event is AuthEvent.UserLoggedIn)
 assertEquals(user, event.user)
 }

/** UserLoggedOuttest that User can be created */ @test
 fun `create UserLoggedOut event`() {
 val event = AuthEvent.UserLoggedOut

 assertTrue(event is AuthEvent)
 assertTrue(event is AuthEvent.UserLoggedOut)
 }

/** UserLoggedInof equality test */ @test
 fun `UserLoggedIn events with same user are equal`() {
 val user = User("npub1" + "a".repeat(59))
 val event1 = AuthEvent.UserLoggedIn(user)
 val event2 = AuthEvent.UserLoggedIn(user)

 assertEquals(event1, event2)
 }

/** differentUserUserLoggedIn not test */ @test
 fun `UserLoggedIn events with different users are not equal`() {
 val user1 = User("npub1" + "a".repeat(59))
 val user2 = User("npub1" + "b".repeat(59))
 val event1 = AuthEvent.UserLoggedIn(user1)
 val event2 = AuthEvent.UserLoggedIn(user2)

 assertNotEquals(event1, event2)
 }

/** UserLoggedOutoftest */ @test
 fun `UserLoggedOut is singleton`() {
 val event1 = AuthEvent.UserLoggedOut
 val event2 = AuthEvent.UserLoggedOut

 assertSame(event1, event2)
 }

/** AuthEventofsealed classtest (whenof) */ @test
 fun `AuthEvent sealed class allows exhaustive when`() {
 val user = User("npub1" + "a".repeat(59))
 val loggedInEvent: AuthEvent = AuthEvent.UserLoggedIn(user)
 val loggedOutEvent: AuthEvent = AuthEvent.UserLoggedOut

// Whena Verify () val loggedInResult =
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
