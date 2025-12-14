package io.github.omochice.pinosu.data.repository

import android.content.Intent
import io.github.omochice.pinosu.data.amber.AmberResponse
import io.github.omochice.pinosu.data.amber.AmberSignerClient
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.LoginError
import io.github.omochice.pinosu.domain.model.error.LogoutError
import io.github.omochice.pinosu.domain.model.error.StorageError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AuthRepositoryの単体テスト
 *
 * Task 5.1: AuthRepositoryの実装
 * - getLoginState(), saveLoginState(), logout()のテスト
 * - loginWithAmber()のテスト（Amber未インストール検出）
 * - AmberResponse処理とローカル保存のフローテスト
 *
 * Requirements: 1.3, 1.4, 2.1, 2.2, 2.4, 2.5
 */
class AuthRepositoryImplTest {

  private lateinit var amberSignerClient: AmberSignerClient
  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var authRepository: AuthRepository

  @Before
  fun setup() {
    amberSignerClient = mockk(relaxed = true)
    localAuthDataSource = mockk(relaxed = true)
    authRepository = AuthRepositoryImpl(amberSignerClient, localAuthDataSource)
  }

  // ========== getLoginState() Tests ==========

  /**
   * ログイン済み状態の取得が成功するテスト
   *
   * Task 5.1: getLoginState()実装 Requirement 2.2: ログイン状態確認
   */
  @Test
  fun testGetLoginState_WhenUserExists_ReturnsUser() = runTest {
    // Given: LocalAuthDataSourceがユーザーを返す
    val expectedUser = User("a".repeat(64))
    coEvery { localAuthDataSource.getUser() } returns expectedUser

    // When: getLoginState()を呼び出す
    val result = authRepository.getLoginState()

    // Then: ユーザーが返される
    assertEquals("Should return user from LocalAuthDataSource", expectedUser, result)
    coVerify { localAuthDataSource.getUser() }
  }

  /**
   * 未ログイン状態の取得テスト
   *
   * Task 5.1: getLoginState()実装 Requirement 2.2: ログイン状態確認
   */
  @Test
  fun testGetLoginState_WhenNoUser_ReturnsNull() = runTest {
    // Given: LocalAuthDataSourceがnullを返す
    coEvery { localAuthDataSource.getUser() } returns null

    // When: getLoginState()を呼び出す
    val result = authRepository.getLoginState()

    // Then: nullが返される
    assertNull("Should return null when no user is stored", result)
    coVerify { localAuthDataSource.getUser() }
  }

  // ========== saveLoginState() Tests ==========

  /**
   * ログイン状態の保存が成功するテスト
   *
   * Task 5.1: saveLoginState()実装 Requirement 1.4: ログイン状態保存
   */
  @Test
  fun testSaveLoginState_Success_ReturnsSuccess() = runTest {
    // Given: LocalAuthDataSourceが正常に保存する
    val user = User("a".repeat(64))
    coEvery { localAuthDataSource.saveUser(user) } returns Unit

    // When: saveLoginState()を呼び出す
    val result = authRepository.saveLoginState(user)

    // Then: Successが返される
    assertTrue("Should return success", result.isSuccess)
    coVerify { localAuthDataSource.saveUser(user) }
  }

  /**
   * ログイン状態の保存が失敗するテスト
   *
   * Task 5.1: saveLoginState()実装 Requirement 5.2: ストレージエラーハンドリング
   */
  @Test
  fun testSaveLoginState_Failure_ReturnsStorageError() = runTest {
    // Given: LocalAuthDataSourceが例外をスロー
    val user = User("a".repeat(64))
    val storageError = StorageError.WriteError("Failed to save")
    coEvery { localAuthDataSource.saveUser(user) } throws storageError

    // When: saveLoginState()を呼び出す
    val result = authRepository.saveLoginState(user)

    // Then: Failureが返される
    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be StorageError", exception is StorageError.WriteError)
    coVerify { localAuthDataSource.saveUser(user) }
  }

  // ========== logout() Tests ==========

  /**
   * ログアウトが成功するテスト
   *
   * Task 5.1: logout()実装 Requirement 2.4: ログアウト機能
   */
  @Test
  fun testLogout_Success_ReturnsSuccess() = runTest {
    // Given: LocalAuthDataSourceが正常にクリアする
    coEvery { localAuthDataSource.clearLoginState() } returns Unit

    // When: logout()を呼び出す
    val result = authRepository.logout()

    // Then: Successが返される
    assertTrue("Should return success", result.isSuccess)
    coVerify { localAuthDataSource.clearLoginState() }
  }

  /**
   * ログアウトが失敗するテスト
   *
   * Task 5.1: logout()実装 Requirement 5.2: ストレージエラーハンドリング
   */
  @Test
  fun testLogout_Failure_ReturnsLogoutError() = runTest {
    // Given: LocalAuthDataSourceが例外をスロー
    val storageError = StorageError.WriteError("Failed to clear")
    coEvery { localAuthDataSource.clearLoginState() } throws storageError

    // When: logout()を呼び出す
    val result = authRepository.logout()

    // Then: Failureが返される
    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(
        "Exception should be LogoutError.StorageError", exception is LogoutError.StorageError)
    coVerify { localAuthDataSource.clearLoginState() }
  }

  // ========== processAmberResponse() Tests ==========

  /**
   * Amberレスポンス処理が成功してローカル保存も成功するテスト
   *
   * Task 5.1: AmberSignerClient → LocalAuthDataSourceフロー Requirement 1.3, 1.4: Amber認証とローカル保存
   */
  @Test
  fun testProcessAmberResponse_Success_SavesUserAndReturnsSuccess() = runTest {
    // Given: Amberが成功レスポンスを返し、ローカル保存も成功
    val pubkey = "a".repeat(64)
    val intent = Intent().apply { putExtra("result", pubkey) }
    val amberResponse = AmberResponse(pubkey, AmberSignerClient.AMBER_PACKAGE_NAME)

    every { amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent) } returns
        Result.success(amberResponse)
    coEvery { localAuthDataSource.saveUser(any()) } returns Unit

    // When: processAmberResponse()を呼び出す
    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: Successが返され、ユーザーが保存される
    assertTrue("Should return success", result.isSuccess)
    val user = result.getOrNull()
    assertNotNull("User should not be null", user)
    assertEquals("Pubkey should match", pubkey, user?.pubkey)
    coVerify { localAuthDataSource.saveUser(any()) }
  }

  /**
   * Amberレスポンスが拒否された場合のテスト
   *
   * Task 5.1: Amberエラーハンドリング Requirement 1.5: エラーハンドリング
   */
  @Test
  fun testProcessAmberResponse_UserRejected_ReturnsLoginError() = runTest {
    // Given: Amberがユーザー拒否を返す
    val intent = Intent().apply { putExtra("rejected", true) }
    every { amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent) } returns
        Result.failure(io.github.omochice.pinosu.data.amber.AmberError.UserRejected)

    // When: processAmberResponse()を呼び出す
    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: UserRejectedエラーが返される
    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.UserRejected", exception is LoginError.UserRejected)
  }

  // ========== checkAmberInstalled() Tests ==========

  /**
   * Amberがインストールされている場合のテスト
   *
   * Task 5.1: Amberインストール確認 Requirement 1.2: Amber未インストール検出
   */
  @Test
  fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {
    // Given: AmberSignerClientがtrueを返す
    every { amberSignerClient.checkAmberInstalled() } returns true

    // When: checkAmberInstalled()を呼び出す
    val result = authRepository.checkAmberInstalled()

    // Then: trueが返される
    assertTrue("Should return true when Amber is installed", result)
  }

  /**
   * Amberがインストールされていない場合のテスト
   *
   * Task 5.1: Amberインストール確認 Requirement 1.2: Amber未インストール検出
   */
  @Test
  fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {
    // Given: AmberSignerClientがfalseを返す
    every { amberSignerClient.checkAmberInstalled() } returns false

    // When: checkAmberInstalled()を呼び出す
    val result = authRepository.checkAmberInstalled()

    // Then: falseが返される
    assertFalse("Should return false when Amber is not installed", result)
  }
}
