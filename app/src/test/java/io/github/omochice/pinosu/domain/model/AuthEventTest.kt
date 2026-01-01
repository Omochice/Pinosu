package io.github.omochice.pinosu.domain.model

import org.junit.Assert.*
import org.junit.Test

/** AuthEvent sealed classのテスト Task 2.1: ドメインモデルの実装（将来拡張用） */
class AuthEventTest {

  /** UserLoggedInイベントを作成できることをテスト */
  @Test
  fun `create UserLoggedIn event`() {
    val user = User("npub1" + "a".repeat(59))
    val event = AuthEvent.UserLoggedIn(user)

    assertTrue(event is AuthEvent)
    assertTrue(event is AuthEvent.UserLoggedIn)
    assertEquals(user, event.user)
  }

  /** UserLoggedOutイベントを作成できることをテスト */
  @Test
  fun `create UserLoggedOut event`() {
    val event = AuthEvent.UserLoggedOut

    assertTrue(event is AuthEvent)
    assertTrue(event is AuthEvent.UserLoggedOut)
  }

  /** UserLoggedInイベントの equality をテスト */
  @Test
  fun `UserLoggedIn events with same user are equal`() {
    val user = User("npub1" + "a".repeat(59))
    val event1 = AuthEvent.UserLoggedIn(user)
    val event2 = AuthEvent.UserLoggedIn(user)

    assertEquals(event1, event2)
  }

  /** 異なるUserを持つUserLoggedInイベントは等しくないことをテスト */
  @Test
  fun `UserLoggedIn events with different users are not equal`() {
    val user1 = User("npub1" + "a".repeat(59))
    val user2 = User("npub1" + "b".repeat(59))
    val event1 = AuthEvent.UserLoggedIn(user1)
    val event2 = AuthEvent.UserLoggedIn(user2)

    assertNotEquals(event1, event2)
  }

  /** UserLoggedOutイベントのシングルトン性をテスト */
  @Test
  fun `UserLoggedOut is singleton`() {
    val event1 = AuthEvent.UserLoggedOut
    val event2 = AuthEvent.UserLoggedOut

    assertSame(event1, event2)
  }

  /** AuthEventのsealed class特性をテスト（when式の網羅性） */
  @Test
  fun `AuthEvent sealed class allows exhaustive when`() {
    val user = User("npub1" + "a".repeat(59))
    val loggedInEvent: AuthEvent = AuthEvent.UserLoggedIn(user)
    val loggedOutEvent: AuthEvent = AuthEvent.UserLoggedOut

    // When式が網羅的であることを確認（コンパイル時チェック）
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
