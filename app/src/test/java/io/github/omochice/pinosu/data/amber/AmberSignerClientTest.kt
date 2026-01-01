package io.github.omochice.pinosu.data.amber

import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * AmberSignerClientの単体テスト
 *
 * Task 4.1: AmberSignerClientの基本実装
 * - checkAmberInstalled()のテスト
 * - AmberResponse、AmberErrorデータクラスの検証
 *
 * Requirements: 1.2, 5.1
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AmberSignerClientTest {

  private lateinit var context: Context

  private lateinit var packageManager: PackageManager

  private lateinit var amberSignerClient: AmberSignerClient

  @Before
  fun setup() {
    context = mockk(relaxed = true)
    packageManager = mockk(relaxed = true)
    every { context.packageManager } returns packageManager
    amberSignerClient = AmberSignerClient(context)
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
    val pubkey = "npub1" + "a".repeat(59)
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
    val response1 = AmberResponse("npub1" + "abc".repeat(19) + "ab", "com.test.app")
    val response2 = AmberResponse("npub1" + "abc".repeat(19) + "ab", "com.test.app")

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

  // ========== createPublicKeyIntent() Tests ==========

  /**
   * createPublicKeyIntent()が正しいスキームのIntentを作成することをテスト
   *
   * Task 4.2: Intent構築（nostrsigner: スキーム） Requirement 4.1: NIP-55プロトコル
   */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectScheme() {
    // When: createPublicKeyIntent()を呼び出す
    val intent = amberSignerClient.createPublicKeyIntent()

    // Then: nostrsignerスキームのURIを持つ
    assertNotNull("Intent should have data URI", intent.data)
    assertEquals(
        "URI scheme should be nostrsigner",
        AmberSignerClient.NOSTRSIGNER_SCHEME,
        intent.data?.scheme)
  }

  /**
   * createPublicKeyIntent()が正しいパッケージ名を設定することをテスト
   *
   * Task 4.2: パッケージ名明示 Requirement 1.3: Amber統合
   */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectPackage() {
    // When: createPublicKeyIntent()を呼び出す
    val intent = amberSignerClient.createPublicKeyIntent()

    // Then: Amberのパッケージ名が設定されている
    assertEquals(
        "Package should be Amber package name",
        AmberSignerClient.AMBER_PACKAGE_NAME,
        intent.`package`)
  }

  /**
   * createPublicKeyIntent()がget_public_keyタイプを設定することをテスト
   *
   * Task 4.2: type: get_public_key設定 Requirement 4.1: NIP-55プロトコル
   */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectType() {
    // When: createPublicKeyIntent()を呼び出す
    val intent = amberSignerClient.createPublicKeyIntent()

    // Then: type extraにget_public_keyが設定されている
    assertEquals(
        "Type extra should be get_public_key",
        AmberSignerClient.TYPE_GET_PUBLIC_KEY,
        intent.getStringExtra("type"))
  }

  /**
   * createPublicKeyIntent()が正しいフラグを設定することをテスト
   *
   * Task 4.2: FLAG_ACTIVITY_SINGLE_TOPとFLAG_ACTIVITY_CLEAR_TOPの設定 Requirement 4.2: Intent設定
   */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectFlags() {
    // When: createPublicKeyIntent()を呼び出す
    val intent = amberSignerClient.createPublicKeyIntent()

    // Then: SINGLE_TOPとCLEAR_TOPフラグが設定されている
    val expectedFlags =
        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP

    // フラグがOR演算で含まれているか確認
    assertTrue(
        "Intent should have SINGLE_TOP and CLEAR_TOP flags",
        (intent.flags and expectedFlags) == expectedFlags)
  }

  /**
   * createPublicKeyIntent()がACTION_VIEWアクションを設定することをテスト
   *
   * Task 4.2: Intent構築
   */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectAction() {
    // When: createPublicKeyIntent()を呼び出す
    val intent = amberSignerClient.createPublicKeyIntent()

    // Then: ACTION_VIEWアクションが設定されている
    assertEquals(
        "Intent action should be ACTION_VIEW", android.content.Intent.ACTION_VIEW, intent.action)
  }

  // ========== handleAmberResponse() Tests ==========

  /**
   * 正常なレスポンス（RESULT_OK + pubkey）を正しく処理するテスト
   *
   * Task 4.3: handleAmberResponse実装 Requirement 1.3: Amberレスポンス処理
   */
  @Test
  fun testHandleAmberResponse_Success_ReturnsAmberResponse() {
    // Given: RESULT_OKと有効なpubkeyを含むIntent
    val pubkey = "npub1" + "a".repeat(59)
    val intent = android.content.Intent()
    intent.putExtra("result", pubkey)

    // When: handleAmberResponse()を呼び出す
    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: Successが返され、pubkeyとpackageNameが正しい
    assertTrue("Should return success", result.isSuccess)
    val response = result.getOrNull()
    assertNotNull("Response should not be null", response)
    assertEquals("Pubkey should match", pubkey, response?.pubkey)
    assertEquals(
        "PackageName should be Amber package",
        AmberSignerClient.AMBER_PACKAGE_NAME,
        response?.packageName)
  }

  /**
   * ユーザー拒否（rejected=true）を検出するテスト
   *
   * Task 4.3: ユーザー拒否検出 Requirement 1.5: エラーハンドリング
   */
  @Test
  fun testHandleAmberResponse_UserRejected_ReturnsError() {
    // Given: rejected=trueを含むIntent
    val intent = android.content.Intent()
    intent.putExtra("rejected", true)

    // When: handleAmberResponse()を呼び出す
    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: UserRejectedエラーが返される
    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is AmberError.UserRejected || error.toString().contains("UserRejected"))
  }

  /**
   * RESULT_CANCELEDの場合にUserRejectedエラーを返すテスト
   *
   * Task 4.3: ユーザー拒否検出 Requirement 1.5: エラーハンドリング
   */
  @Test
  fun testHandleAmberResponse_ResultCanceled_ReturnsUserRejected() {
    // Given: RESULT_CANCELED
    val intent = android.content.Intent()

    // When: handleAmberResponse()を呼び出す
    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_CANCELED, intent)

    // Then: UserRejectedエラーが返される
    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is AmberError.UserRejected || error.toString().contains("UserRejected"))
  }

  /**
   * Intentがnullの場合にInvalidResponseエラーを返すテスト
   *
   * Task 4.3: 不正レスポンスのエラーハンドリング Requirement 5.3: エラーログ記録
   */
  @Test
  fun testHandleAmberResponse_NullIntent_ReturnsInvalidResponse() {
    // Given: null Intent
    // When: handleAmberResponse()を呼び出す
    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, null)

    // Then: InvalidResponseエラーが返される
    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /**
   * resultが空文字列の場合にInvalidResponseエラーを返すテスト
   *
   * Task 4.3: 不正レスポンスのエラーハンドリング Requirement 5.3: エラーログ記録
   */
  @Test
  fun testHandleAmberResponse_EmptyResult_ReturnsInvalidResponse() {
    // Given: 空のresult
    val intent = android.content.Intent()
    intent.putExtra("result", "")

    // When: handleAmberResponse()を呼び出す
    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: InvalidResponseエラーが返される
    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /**
   * resultが不正な形式（npub1で始まらない）の場合にInvalidResponseエラーを返すテスト
   *
   * Task 4.3: 不正レスポンスのエラーハンドリング Requirement 5.3: エラーログ記録
   */
  @Test
  fun testHandleAmberResponse_InvalidPubkeyLength_ReturnsInvalidResponse() {
    // Given: 不正な形式のpubkey（npub1で始まらない）
    val intent = android.content.Intent()
    intent.putExtra("result", "a".repeat(64))

    // When: handleAmberResponse()を呼び出す
    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: InvalidResponseエラーが返される
    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /**
   * resultが不正な形式（nsec1で始まる）の場合にInvalidResponseエラーを返すテスト
   *
   * Task 4.3: 不正レスポンスのエラーハンドリング Requirement 5.3: エラーログ記録
   */
  @Test
  fun testHandleAmberResponse_InvalidPubkeyFormat_ReturnsInvalidResponse() {
    // Given: 秘密鍵形式のpubkey（nsec1で始まる）
    val intent = android.content.Intent()
    intent.putExtra("result", "nsec1" + "a".repeat(59))

    // When: handleAmberResponse()を呼び出す
    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: InvalidResponseエラーが返される
    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  // ========== maskPubkey() Tests ==========

  /**
   * Bech32形式のpubkeyを正しくマスキングするテスト
   *
   * Task 4.4: pubkeyマスキング関数の実装 Requirement 6.3: センシティブデータのログマスキング
   */
  @Test
  fun testMaskPubkey_ValidPubkey_ReturnsMaskedString() {
    // Given: Bech32形式のpubkey
    val pubkey = "npub1" + "abcdef0123456789".repeat(3) + "abcdef01234"

    // When: maskPubkey()を呼び出す
    val masked = amberSignerClient.maskPubkey(pubkey)

    // Then: 最初8文字 + "..." + 最後8文字の形式でマスキングされる
    assertEquals("Should mask pubkey as first8...last8", "npub1abc...def01234", masked)
  }

  /**
   * 異なるpubkeyでもマスキング形式が一貫していることをテスト
   *
   * Task 4.4: pubkeyマスキング関数の実装 Requirement 6.3: センシティブデータのログマスキング
   */
  @Test
  fun testMaskPubkey_DifferentPubkey_ReturnsMaskedString() {
    // Given: 別のBech32形式のpubkey
    val pubkey = "npub1" + "1234567890abcdef".repeat(3) + "1234567890a"

    // When: maskPubkey()を呼び出す
    val masked = amberSignerClient.maskPubkey(pubkey)

    // Then: 最初8文字 + "..." + 最後8文字の形式でマスキングされる
    assertEquals("Should mask pubkey as first8...last8", "npub1123...4567890a", masked)
  }

  /**
   * 短いpubkey（64文字未満）に対するマスキングのテスト
   *
   * Task 4.4: pubkeyマスキング関数の実装 Requirement 6.3: センシティブデータのログマスキング
   */
  @Test
  fun testMaskPubkey_ShortPubkey_ReturnsOriginalString() {
    // Given: 16文字以下の短いpubkey
    val pubkey = "abcdef0123456789"

    // When: maskPubkey()を呼び出す
    val masked = amberSignerClient.maskPubkey(pubkey)

    // Then: マスキングせずに元の文字列を返す
    assertEquals("Should return original string when pubkey is too short", pubkey, masked)
  }

  /**
   * 空文字列に対するマスキングのテスト
   *
   * Task 4.4: pubkeyマスキング関数の実装 Requirement 6.3: センシティブデータのログマスキング
   */
  @Test
  fun testMaskPubkey_EmptyString_ReturnsEmptyString() {
    // Given: 空文字列
    val pubkey = ""

    // When: maskPubkey()を呼び出す
    val masked = amberSignerClient.maskPubkey(pubkey)

    // Then: 空文字列を返す
    assertEquals("Should return empty string when input is empty", "", masked)
  }

  /**
   * マスキング結果の長さが正しいことをテスト
   *
   * Task 4.4: pubkeyマスキング関数の実装 Requirement 6.3: センシティブデータのログマスキング
   */
  @Test
  fun testMaskPubkey_ResultLength_IsCorrect() {
    // Given: Bech32形式のpubkey
    val pubkey = "npub1" + "a".repeat(59)

    // When: maskPubkey()を呼び出す
    val masked = amberSignerClient.maskPubkey(pubkey)

    // Then: 結果は19文字（8 + 3 + 8）である
    assertEquals("Masked string should be 19 characters (8+3+8)", 19, masked.length)
  }
}
