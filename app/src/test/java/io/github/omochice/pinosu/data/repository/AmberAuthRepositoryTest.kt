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
 * AmberAuthRepositoryの単体テスト
 * - getLoginState(), saveLoginState(), logout()のテスト
 * - loginWithAmber()のテスト（Amber未インストール検出）
 * - AmberResponse処理とローカル保存のフローテスト
 * - Amber成功 → ローカル保存成功の正常系テスト ✓
 * - Amber失敗時のエラー分類テスト ✓
 * - ログアウト処理テスト ✓
 * - トランザクション整合性テスト ✓
 */
class AmberAuthRepositoryTest {

  private lateinit var amberSignerClient: AmberSignerClient
  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var authRepository: AuthRepository

  @Before
  fun setup() {
    amberSignerClient = mockk(relaxed = true)
    localAuthDataSource = mockk(relaxed = true)
    authRepository = AmberAuthRepository(amberSignerClient, localAuthDataSource)
  }

  // ========== getLoginState() Tests ==========

  /** ログイン済み状態の取得が成功するテスト */
  @Test
  fun testGetLoginState_WhenUserExists_ReturnsUser() = runTest {
    // Given: LocalAuthDataSourceがユーザーを返す
    val expectedUser = User("npub1" + "a".repeat(59))
    coEvery { localAuthDataSource.getUser() } returns expectedUser

    // When: getLoginState()を呼び出す
    val result = authRepository.getLoginState()

    // Then: ユーザーが返される
    assertEquals("Should return user from LocalAuthDataSource", expectedUser, result)
    coVerify { localAuthDataSource.getUser() }
  }

  /** 未ログイン状態の取得テスト */
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

  /** ログイン状態の保存が成功するテスト */
  @Test
  fun testSaveLoginState_Success_ReturnsSuccess() = runTest {
    // Given: LocalAuthDataSourceが正常に保存する
    val user = User("npub1" + "a".repeat(59))
    coEvery { localAuthDataSource.saveUser(user) } returns Unit

    // When: saveLoginState()を呼び出す
    val result = authRepository.saveLoginState(user)

    // Then: Successが返される
    assertTrue("Should return success", result.isSuccess)
    coVerify { localAuthDataSource.saveUser(user) }
  }

  /** ログイン状態の保存が失敗するテスト */
  @Test
  fun testSaveLoginState_Failure_ReturnsStorageError() = runTest {
    // Given: LocalAuthDataSourceが例外をスロー
    val user = User("npub1" + "a".repeat(59))
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

  /** ログアウトが成功するテスト */
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

  /** ログアウトが失敗するテスト */
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

  /** Amberレスポンス処理が成功してローカル保存も成功するテスト */
  @Test
  fun testProcessAmberResponse_Success_SavesUserAndReturnsSuccess() = runTest {
    // Given: Amberが成功レスポンスを返し、ローカル保存も成功
    val pubkey = "npub1" + "a".repeat(59)
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

  /** Amberレスポンスが拒否された場合のテスト */
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

  /** Amber未インストールエラーの分類テスト */
  @Test
  fun testProcessAmberResponse_AmberNotInstalled_ReturnsAmberNotInstalledError() = runTest {
    // Given: AmberがNotInstalledエラーを返す
    val intent = Intent()
    every { amberSignerClient.handleAmberResponse(any(), any()) } returns
        Result.failure(io.github.omochice.pinosu.data.amber.AmberError.NotInstalled)

    // When: processAmberResponse()を呼び出す
    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: LoginError.AmberNotInstalledが返される
    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(
        "Exception should be LoginError.AmberNotInstalled",
        exception is LoginError.AmberNotInstalled)
  }

  /** Amberタイムアウトエラーの分類テスト */
  @Test
  fun testProcessAmberResponse_Timeout_ReturnsTimeoutError() = runTest {
    // Given: AmberがTimeoutエラーを返す
    val intent = Intent()
    every { amberSignerClient.handleAmberResponse(any(), any()) } returns
        Result.failure(io.github.omochice.pinosu.data.amber.AmberError.Timeout)

    // When: processAmberResponse()を呼び出す
    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: LoginError.Timeoutが返される
    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.Timeout", exception is LoginError.Timeout)
  }

  /** Amber InvalidResponseエラーの分類テスト（NetworkErrorとして扱う） */
  @Test
  fun testProcessAmberResponse_InvalidResponse_ReturnsNetworkError() = runTest {
    // Given: AmberがInvalidResponseエラーを返す
    val intent = Intent()
    every { amberSignerClient.handleAmberResponse(any(), any()) } returns
        Result.failure(
            io.github.omochice.pinosu.data.amber.AmberError.InvalidResponse("Invalid data"))

    // When: processAmberResponse()を呼び出す
    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: LoginError.NetworkErrorが返される
    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.NetworkError", exception is LoginError.NetworkError)
  }

  /** トランザクション整合性テスト: Amber成功 → ローカル保存失敗 */
  @Test
  fun testProcessAmberResponse_AmberSuccess_LocalStorageFail_ReturnsUnknownError() = runTest {
    // Given: Amberは成功するがローカル保存が失敗
    val pubkey = "npub1" + "a".repeat(59)
    val intent = Intent().apply { putExtra("result", pubkey) }
    val amberResponse = AmberResponse(pubkey, AmberSignerClient.AMBER_PACKAGE_NAME)

    every { amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent) } returns
        Result.success(amberResponse)
    coEvery { localAuthDataSource.saveUser(any()) } throws StorageError.WriteError("Storage full")

    // When: processAmberResponse()を呼び出す
    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    // Then: LoginError.UnknownErrorが返される（StorageErrorをラップ）
    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.UnknownError", exception is LoginError.UnknownError)

    // StorageErrorが原因として含まれていることを確認
    val unknownError = exception as LoginError.UnknownError
    assertTrue("Cause should be StorageError", unknownError.throwable is StorageError.WriteError)
  }

  // ========== checkAmberInstalled() Tests ==========

  /** Amberがインストールされている場合のテスト */
  @Test
  fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {
    // Given: AmberSignerClientがtrueを返す
    every { amberSignerClient.checkAmberInstalled() } returns true

    // When: checkAmberInstalled()を呼び出す
    val result = authRepository.checkAmberInstalled()

    // Then: trueが返される
    assertTrue("Should return true when Amber is installed", result)
  }

  /** Amberがインストールされていない場合のテスト */
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
