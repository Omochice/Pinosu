package io.github.omochice.pinosu.domain.model

import org.junit.Assert.*
import org.junit.Test

/** Userエンティティのテスト Task 2.1: ドメインモデルの実装 */
class UserTest {

  /** 正常なpubkey（Bech32形式）でUserを作成できることをテスト */
  @Test
  fun `create User with valid pubkey`() {
    val validPubkey = "npub1" + "a".repeat(59)
    val user = User(validPubkey)

    assertEquals(validPubkey, user.pubkey)
  }

  /** 別の正常なpubkey（Bech32形式、数字と小文字を含む）でUserを作成できることをテスト */
  @Test
  fun `create User with valid hex pubkey containing numbers`() {
    val validPubkey = "npub1" + "0123456789abcdef".repeat(3) + "0123456789a"
    val user = User(validPubkey)

    assertTrue(user.pubkey.startsWith("npub1"))
    assertEquals(validPubkey, user.pubkey)
  }

  /** npub1で始まらないpubkeyでIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with pubkey too short throws exception`() {
    val shortPubkey = "npub" + "a".repeat(59) // npub1ではなくnpubで始まる
    User(shortPubkey)
  }

  /** 16進数形式（旧形式）のpubkeyでIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with pubkey too long throws exception`() {
    val longPubkey = "a".repeat(64) // npub1で始まらない16進数形式
    User(longPubkey)
  }

  /** npub1で始まらないpubkey（大文字NPUB1）でIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with uppercase characters throws exception`() {
    val invalidPubkey = "NPUB1" + "A".repeat(59) // 大文字のNPUB1
    User(invalidPubkey)
  }

  /** nsecで始まるpubkey（秘密鍵形式）でIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with non-hex characters throws exception`() {
    val invalidPubkey = "nsec1" + "g".repeat(59) // nsec1は秘密鍵の形式
    User(invalidPubkey)
  }

  /** 空のpubkeyでIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with empty pubkey throws exception`() {
    User("")
  }

  /** nprofile1で始まるpubkey（プロフィール形式）でIllegalArgumentExceptionがスローされることをテスト */
  @Test(expected = IllegalArgumentException::class)
  fun `create User with spaces throws exception`() {
    val invalidPubkey = "nprofile1" + "a".repeat(54) // nprofile1はプロフィール形式
    User(invalidPubkey)
  }

  /** Userのequality（data classの性質）をテスト */
  @Test
  fun `User equality works correctly`() {
    val pubkey = "npub1" + "a".repeat(59)
    val user1 = User(pubkey)
    val user2 = User(pubkey)

    assertEquals(user1, user2)
    assertEquals(user1.hashCode(), user2.hashCode())
  }

  /** 異なるpubkeyを持つUserは等しくないことをテスト */
  @Test
  fun `Users with different pubkeys are not equal`() {
    val user1 = User("npub1" + "a".repeat(59))
    val user2 = User("npub1" + "b".repeat(59))

    assertNotEquals(user1, user2)
  }
}
