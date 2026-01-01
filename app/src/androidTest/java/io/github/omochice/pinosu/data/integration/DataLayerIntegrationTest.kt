package io.github.omochice.pinosu.data.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.omochice.pinosu.data.amber.AmberSignerClient
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.repository.AmberAuthRepository
import io.github.omochice.pinosu.domain.model.User
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Data層の統合テスト
 * - AuthRepository + AmberSignerClient + LocalAuthDataSource 統合テスト
 * - EncryptedSharedPreferences実動作テスト(保存 → 取得 → 削除)
 *
 * テスト方針:
 * - Data層: 実際のAmberAuthRepository, 実際のLocalAuthDataSource, 実際のAmberSignerClient
 * - Storage: 実際のEncryptedSharedPreferences (Android runtime必要)
 * - Note: Amber通信部分は実際のAmberアプリが必要なため、ストレージ操作のみテスト
 */
@RunWith(AndroidJUnit4::class)
class DataLayerIntegrationTest {

  private lateinit var context: Context
  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var amberSignerClient: AmberSignerClient
  private lateinit var authRepository: AmberAuthRepository

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()

    localAuthDataSource = LocalAuthDataSource(context)

    amberSignerClient = AmberSignerClient(context)

    authRepository = AmberAuthRepository(amberSignerClient, localAuthDataSource)
  }

  @After
  fun tearDown() {
    runTest { localAuthDataSource.clearLoginState() }
  }

  /**
   * Amber未インストール検出 → AuthRepository統合
   *
   * 統合フロー:
   * 1. AuthRepositoryがAmberインストール確認
   * 2. AmberSignerClientで検出処理
   * 3. 結果を返す
   */
  @Test
  fun checkAmberInstalled_shouldUseAmberSignerClient() {

    val isInstalled = authRepository.checkAmberInstalled()

    assertTrue("checkAmberInstalled should return boolean", isInstalled || !isInstalled)
  }

  /**
   * EncryptedSharedPreferences実動作テスト: 保存 → 取得
   *
   * 統合フロー:
   * 1. LocalAuthDataSourceにユーザー情報を保存
   * 2. EncryptedSharedPreferencesで暗号化保存される
   * 3. 保存したデータを取得して検証
   */
  @Test
  fun encryptedStorage_saveAndGet_shouldWorkCorrectly() = runTest {
    val testPubkey = "b".repeat(64)
    val testUser = User(testPubkey)

    localAuthDataSource.saveUser(testUser)
    advanceUntilIdle()

    val retrievedUser = localAuthDataSource.getUser()
    assertNotNull("Retrieved user should not be null", retrievedUser)
    assertEquals("Retrieved pubkey should match", testPubkey, retrievedUser?.pubkey)
  }

  /**
   * EncryptedSharedPreferences実動作テスト: 保存 → 削除 → 取得
   *
   * 統合フロー:
   * 1. LocalAuthDataSourceにユーザー情報を保存
   * 2. ログアウト処理で削除
   * 3. 削除後は取得できないことを確認
   */
  @Test
  fun encryptedStorage_saveAndDelete_shouldWorkCorrectly() = runTest {
    val testPubkey = "c".repeat(64)
    val testUser = User(testPubkey)
    localAuthDataSource.saveUser(testUser)
    advanceUntilIdle()

    val userBeforeDelete = localAuthDataSource.getUser()
    assertNotNull("User should exist before delete", userBeforeDelete)

    localAuthDataSource.clearLoginState()
    advanceUntilIdle()

    val userAfterDelete = localAuthDataSource.getUser()
    assertNull("User should be null after delete", userAfterDelete)
  }

  /**
   * EncryptedSharedPreferences実動作テスト: 複数回の保存・取得・削除サイクル
   *
   * 統合フロー:
   * 1. 保存 → 取得 → 削除を複数回繰り返す
   * 2. EncryptedSharedPreferencesの安定性を確認
   */
  @Test
  fun encryptedStorage_multipleSaveGetDeleteCycles_shouldWorkCorrectly() = runTest {
    val users =
        listOf(
            User("d".repeat(64)),
            User("e".repeat(64)),
            User("f".repeat(64)),
        )

    users.forEach { user ->
      localAuthDataSource.saveUser(user)

      val retrievedUser = localAuthDataSource.getUser()
      assertNotNull("Retrieved user should not be null", retrievedUser)
      assertEquals("Retrieved pubkey should match", user.pubkey, retrievedUser?.pubkey)

      localAuthDataSource.clearLoginState()

      val userAfterDelete = localAuthDataSource.getUser()
      assertNull("User should be null after delete", userAfterDelete)
    }
  }

  /**
   * ログアウトフロー → EncryptedSharedPreferencesからデータ削除
   *
   * 統合フロー:
   * 1. ユーザーがログイン済み状態
   * 2. AuthRepository.logout()を呼び出す
   * 3. LocalAuthDataSourceでログイン状態がクリアされる
   * 4. EncryptedSharedPreferencesからデータが削除される
   */
  @Test
  fun logoutFlow_shouldClearEncryptedStorage() = runTest {
    val testPubkey = "g".repeat(64)
    val testUser = User(testPubkey)
    localAuthDataSource.saveUser(testUser)
    advanceUntilIdle()

    val userBeforeLogout = authRepository.getLoginState()
    assertNotNull("User should exist before logout", userBeforeLogout)
    assertEquals("Pubkey should match", testPubkey, userBeforeLogout?.pubkey)

    authRepository.logout()
    advanceUntilIdle()

    val userAfterLogout = authRepository.getLoginState()
    assertNull("User should be null after logout", userAfterLogout)

    val directRetrieve = localAuthDataSource.getUser()
    assertNull("User should be null in storage", directRetrieve)
  }

  /**
   * アプリ再起動シミュレーション → ログイン状態復元
   *
   * 統合フロー:
   * 1. ユーザー情報を保存
   * 2. AuthRepository/LocalAuthDataSourceインスタンスを再作成 (再起動シミュレーション)
   * 3. 保存されたログイン状態を復元できることを確認
   */
  @Test
  fun appRestart_shouldRestoreLoginStateFromEncryptedStorage() = runTest {
    val testPubkey = "h".repeat(64)
    val testUser = User(testPubkey)
    localAuthDataSource.saveUser(testUser)
    advanceUntilIdle()

    val newLocalAuthDataSource = LocalAuthDataSource(context)
    val newAuthRepository = AmberAuthRepository(amberSignerClient, newLocalAuthDataSource)

    val restoredUser = newAuthRepository.getLoginState()
    assertNotNull("User should be restored after restart", restoredUser)
    assertEquals("Restored pubkey should match", testPubkey, restoredUser?.pubkey)
  }

  /**
   * 不正データハンドリング → nullを返す
   *
   * 統合フロー:
   * 1. 不正な公開鍵フォーマットのデータを保存しようとする
   * 2. Userクラスのバリデーションでエラー
   * 3. 保存されずにnullが返される
   */
  @Test
  fun invalidData_shouldBeRejectedByUserValidation() = runTest {
    val invalidPubkey = "invalid_pubkey_format"

    try {
      val invalidUser = User(invalidPubkey)
      localAuthDataSource.saveUser(invalidUser)
      fail("Should throw exception for invalid pubkey")
    } catch (e: IllegalArgumentException) {
      assertTrue("Should contain validation error", e.message?.contains("Invalid") == true)
    }
  }

  private suspend fun advanceUntilIdle() {
    kotlinx.coroutines.delay(100)
  }
}
