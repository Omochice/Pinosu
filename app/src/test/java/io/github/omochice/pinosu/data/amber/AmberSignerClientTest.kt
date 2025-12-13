package io.github.omochice.pinosu.data.amber

import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AmberSignerClientの単体テスト
 *
 * Task 4.1: AmberSignerClientの基本実装
 * - checkAmberInstalled()のテスト
 * - AmberResponse、AmberErrorデータクラスの検証
 *
 * Requirements: 1.2, 5.1
 */
class AmberSignerClientTest {

  private lateinit var context: Context

  private lateinit var packageManager: PackageManager

  private lateinit var amberSignerClient: AmberSignerClient

  @Before
  fun setup() {
    context = mockk(relaxed = true)
    packageManager = mockk(relaxed = true)
    every { context.packageManager } returns packageManager
  }

  // ========== checkAmberInstalled() Tests ==========

  /**
   * Amberがインストールされている場合にtrueを返すテスト
   *
   * Task 4.1: checkAmberInstalled()実装 Requirement 1.2: Amber未インストール検出
   */
  @Test
  fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {
    // Given: Amberがインストールされている
    every {
      packageManager.getPackageInfo(
          AmberSignerClient.AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } returns android.content.pm.PackageInfo()

    amberSignerClient = AmberSignerClient(context)

    // When: checkAmberInstalled()を呼び出す
    val result = amberSignerClient.checkAmberInstalled()

    // Then: trueが返される
    assertTrue("Should return true when Amber is installed", result)
  }

  /**
   * Amberがインストールされていない場合にfalseを返すテスト
   *
   * Task 4.1: Amber未インストール検出ロジック Requirement 1.2: Amber未インストール検出
   */
  @Test
  fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {
    // Given: Amberがインストールされていない（PackageManager.NameNotFoundException）
    every {
      packageManager.getPackageInfo(
          AmberSignerClient.AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws PackageManager.NameNotFoundException()

    amberSignerClient = AmberSignerClient(context)

    // When: checkAmberInstalled()を呼び出す
    val result = amberSignerClient.checkAmberInstalled()

    // Then: falseが返される
    assertFalse("Should return false when Amber is not installed", result)
  }

  /**
   * PackageManager例外発生時にfalseを返すテスト
   *
   * Task 4.1: エラーハンドリング
   */
  @Test
  fun testCheckAmberInstalled_OnException_ReturnsFalse() {
    // Given: PackageManagerが例外をスロー
    every {
      packageManager.getPackageInfo(
          AmberSignerClient.AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws RuntimeException("Unexpected error")

    amberSignerClient = AmberSignerClient(context)

    // When: checkAmberInstalled()を呼び出す
    val result = amberSignerClient.checkAmberInstalled()

    // Then: falseが返される
    assertFalse("Should return false on exception", result)
  }

  // ========== AmberResponse Data Class Tests ==========

  /**
   * AmberResponseデータクラスが正しく構築されることをテスト
   *
   * Task 4.1: AmberResponseデータクラスの定義
   */
  @Test
  fun testAmberResponse_Construction() {
    // Given: 有効なpubkeyとpackageName
    val pubkey = "a".repeat(64)
    val packageName = "com.greenart7c3.nostrsigner"

    // When: AmberResponseを構築
    val response = AmberResponse(pubkey, packageName)

    // Then: プロパティが正しく設定される
    assertEquals("Pubkey should match", pubkey, response.pubkey)
    assertEquals("PackageName should match", packageName, response.packageName)
  }

  /**
   * AmberResponseが等価性を正しく判定することをテスト
   *
   * Task 4.1: データクラスの検証
   */
  @Test
  fun testAmberResponse_Equality() {
    // Given: 同じ値を持つ2つのAmberResponse
    val response1 = AmberResponse("abc".repeat(21) + "a", "com.test.app")
    val response2 = AmberResponse("abc".repeat(21) + "a", "com.test.app")

    // Then: 等価である
    assertEquals("Responses with same values should be equal", response1, response2)
  }

  // ========== AmberError Sealed Class Tests ==========

  /**
   * AmberError.NotInstalledが正しく構築されることをテスト
   *
   * Task 4.1: AmberErrorの定義 Requirement 5.1: Amber未検出時のエラー
   */
  @Test
  fun testAmberError_NotInstalled() {
    // When: NotInstalledエラーを作成
    val error: AmberError = AmberError.NotInstalled

    // Then: 正しい型である
    assertTrue("Should be NotInstalled type", error is AmberError.NotInstalled)
  }

  /**
   * AmberError.UserRejectedが正しく構築されることをテスト
   *
   * Task 4.1: AmberErrorの定義
   */
  @Test
  fun testAmberError_UserRejected() {
    // When: UserRejectedエラーを作成
    val error: AmberError = AmberError.UserRejected

    // Then: 正しい型である
    assertTrue("Should be UserRejected type", error is AmberError.UserRejected)
  }

  /**
   * AmberError.Timeoutが正しく構築されることをテスト
   *
   * Task 4.1: AmberErrorの定義
   */
  @Test
  fun testAmberError_Timeout() {
    // When: Timeoutエラーを作成
    val error: AmberError = AmberError.Timeout

    // Then: 正しい型である
    assertTrue("Should be Timeout type", error is AmberError.Timeout)
  }

  /**
   * AmberError.InvalidResponseが正しく構築されることをテスト
   *
   * Task 4.1: AmberErrorの定義
   */
  @Test
  fun testAmberError_InvalidResponse() {
    // Given: エラーメッセージ
    val message = "Invalid response format"

    // When: InvalidResponseエラーを作成
    val error: AmberError = AmberError.InvalidResponse(message)

    // Then: 正しい型とメッセージを持つ
    assertTrue("Should be InvalidResponse type", error is AmberError.InvalidResponse)
    assertEquals("Message should match", message, (error as AmberError.InvalidResponse).message)
  }

  /**
   * AmberError.IntentResolutionErrorが正しく構築されることをテスト
   *
   * Task 4.1: AmberErrorの定義
   */
  @Test
  fun testAmberError_IntentResolutionError() {
    // Given: エラーメッセージ
    val message = "Cannot resolve intent"

    // When: IntentResolutionErrorエラーを作成
    val error: AmberError = AmberError.IntentResolutionError(message)

    // Then: 正しい型とメッセージを持つ
    assertTrue("Should be IntentResolutionError type", error is AmberError.IntentResolutionError)
    assertEquals(
        "Message should match", message, (error as AmberError.IntentResolutionError).message)
  }
}
