package io.github.omochice.pinosu.domain.model

import org.junit.Assert.*
import org.junit.Test

/** Userエンティティのテスト Task 2.1: ドメインモデルの実装 */
class UserTest {

  /** 正常なpubkey（64文字の16進数）でUserを作成できることをテスト */
  @Test
  fun `create User with valid pubkey`() {
    val validPubkey = "a".repeat(64)
    val user = User(validPubkey)

    assertEquals(validPubkey, user.pubkey)
  }

  /** 別の正常なpubkey（数字と小文字a-fの組み合わせ）でUserを作成できることをテスト */
  @Test
  fun `create User with valid hex pubkey containing numbers`() {
    val validPubkey = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
    val user = User(validPubkey)

    assertEquals(64, user.pubkey.length)
    assertEquals(validPubkey, user.pubkey)
  }

  /** 63文字のpubkeyでIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with pubkey too short throws exception`() {
    val shortPubkey = "a".repeat(63)
    User(shortPubkey)
  }

  /** 65文字のpubkeyでIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with pubkey too long throws exception`() {
    val longPubkey = "a".repeat(65)
    User(longPubkey)
  }

  /** 大文字を含むpubkeyでIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with uppercase characters throws exception`() {
    val invalidPubkey = "A".repeat(64)
    User(invalidPubkey)
  }

  /** 16進数以外の文字を含むpubkeyでIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with non-hex characters throws exception`() {
    val invalidPubkey = "g".repeat(64)
    User(invalidPubkey)
  }

  /** 空のpubkeyでIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with empty pubkey throws exception`() {
    User("")
  }

  /** スペースを含むpubkeyでIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with spaces throws exception`() {
    val invalidPubkey = "a".repeat(32) + " " + "a".repeat(31)
    User(invalidPubkey)
  }

  /** Userのequality（data classの性質）をテスト */
  @Test
  fun `User equality works correctly`() {
    val pubkey = "a".repeat(64)
    val user1 = User(pubkey)
    val user2 = User(pubkey)

    assertEquals(user1, user2)
    assertEquals(user1.hashCode(), user2.hashCode())
  }

  /** 異なるpubkeyを持つUserは等しくないことをテスト */
  @Test
  fun `Users with different pubkeys are not equal`() {
    val user1 = User("a".repeat(64))
    val user2 = User("b".repeat(64))

    assertNotEquals(user1, user2)
  }
}
