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
 * Unit tests for AmberAuthRepository
 * - getLoginState(), saveLoginState(), logout() test
 * - loginWithAmber() test (Amber not installed detection)
 * - AmberResponse processing and local save flow test
 * - Amber success → local save success happy path test ✓
 * - Amber failure error classification test ✓
 * - Logout handling test ✓
 * - Transaction consistency test ✓
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

  /** Test successful retrieval of logged-in state */
  @Test
  fun testGetLoginState_WhenUserExists_ReturnsUser() = runTest {
    val expectedUser = User("npub1" + "a".repeat(59))
    coEvery { localAuthDataSource.getUser() } returns expectedUser

    val result = authRepository.getLoginState()

    assertEquals("Should return user from LocalAuthDataSource", expectedUser, result)
    coVerify { localAuthDataSource.getUser() }
  }

  /** Test retrieval of not-logged-in state */
  @Test
  fun testGetLoginState_WhenNoUser_ReturnsNull() = runTest {
    coEvery { localAuthDataSource.getUser() } returns null

    val result = authRepository.getLoginState()

    assertNull("Should return null when no user is stored", result)
    coVerify { localAuthDataSource.getUser() }
  }

  /** Test successful save of login state */
  @Test
  fun testSaveLoginState_Success_ReturnsSuccess() = runTest {
    val user = User("npub1" + "a".repeat(59))
    coEvery { localAuthDataSource.saveUser(user) } returns Unit

    val result = authRepository.saveLoginState(user)

    assertTrue("Should return success", result.isSuccess)
    coVerify { localAuthDataSource.saveUser(user) }
  }

  /** Test that login state save fails */
  @Test
  fun testSaveLoginState_Failure_ReturnsStorageError() = runTest {
    val user = User("npub1" + "a".repeat(59))
    val storageError = StorageError.WriteError("Failed to save")
    coEvery { localAuthDataSource.saveUser(user) } throws storageError

    val result = authRepository.saveLoginState(user)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be StorageError", exception is StorageError.WriteError)
    coVerify { localAuthDataSource.saveUser(user) }
  }

  /** Test successful logout */
  @Test
  fun testLogout_Success_ReturnsSuccess() = runTest {
    coEvery { localAuthDataSource.clearLoginState() } returns Unit

    val result = authRepository.logout()

    assertTrue("Should return success", result.isSuccess)
    coVerify { localAuthDataSource.clearLoginState() }
  }

  /** Test that logout fails */
  @Test
  fun testLogout_Failure_ReturnsLogoutError() = runTest {
    val storageError = StorageError.WriteError("Failed to clear")
    coEvery { localAuthDataSource.clearLoginState() } throws storageError

    val result = authRepository.logout()

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(
        "Exception should be LogoutError.StorageError", exception is LogoutError.StorageError)
    coVerify { localAuthDataSource.clearLoginState() }
  }

  /** Test Amber response success and local save success */
  @Test
  fun testProcessAmberResponse_Success_SavesUserAndReturnsSuccess() = runTest {
    val pubkey = "npub1" + "a".repeat(59)
    val intent = Intent().apply { putExtra("result", pubkey) }
    val amberResponse = AmberResponse(pubkey, AmberSignerClient.AMBER_PACKAGE_NAME)

    every { amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent) } returns
        Result.success(amberResponse)
    coEvery { localAuthDataSource.saveUser(any()) } returns Unit

    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return success", result.isSuccess)
    val user = result.getOrNull()
    assertNotNull("User should not be null", user)
    assertEquals("Pubkey should match", pubkey, user?.pubkey)
    coVerify { localAuthDataSource.saveUser(any()) }
  }

  /** Test Amber response rejection */
  @Test
  fun testProcessAmberResponse_UserRejected_ReturnsLoginError() = runTest {
    val intent = Intent().apply { putExtra("rejected", true) }
    every { amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent) } returns
        Result.failure(io.github.omochice.pinosu.data.amber.AmberError.UserRejected)

    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.UserRejected", exception is LoginError.UserRejected)
  }

  /** Test Amber not installed error classification */
  @Test
  fun testProcessAmberResponse_AmberNotInstalled_ReturnsAmberNotInstalledError() = runTest {
    val intent = Intent()
    every { amberSignerClient.handleAmberResponse(any(), any()) } returns
        Result.failure(io.github.omochice.pinosu.data.amber.AmberError.NotInstalled)

    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(
        "Exception should be LoginError.AmberNotInstalled",
        exception is LoginError.AmberNotInstalled)
  }

  /** Test Amber timeout error classification */
  @Test
  fun testProcessAmberResponse_Timeout_ReturnsTimeoutError() = runTest {
    val intent = Intent()
    every { amberSignerClient.handleAmberResponse(any(), any()) } returns
        Result.failure(io.github.omochice.pinosu.data.amber.AmberError.Timeout)

    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.Timeout", exception is LoginError.Timeout)
  }

  /** Test Amber InvalidResponse error classification（treated as NetworkError） */
  @Test
  fun testProcessAmberResponse_InvalidResponse_ReturnsNetworkError() = runTest {
    val intent = Intent()
    every { amberSignerClient.handleAmberResponse(any(), any()) } returns
        Result.failure(
            io.github.omochice.pinosu.data.amber.AmberError.InvalidResponse("Invalid data"))

    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.NetworkError", exception is LoginError.NetworkError)
  }

  /** Transaction consistency test: Amber success → local save failure */
  @Test
  fun testProcessAmberResponse_AmberSuccess_LocalStorageFail_ReturnsUnknownError() = runTest {
    val pubkey = "npub1" + "a".repeat(59)
    val intent = Intent().apply { putExtra("result", pubkey) }
    val amberResponse = AmberResponse(pubkey, AmberSignerClient.AMBER_PACKAGE_NAME)

    every { amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent) } returns
        Result.success(amberResponse)
    coEvery { localAuthDataSource.saveUser(any()) } throws StorageError.WriteError("Storage full")

    val result = authRepository.processAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.UnknownError", exception is LoginError.UnknownError)

    // Verify StorageError is included as cause
    val unknownError = exception as LoginError.UnknownError
    assertTrue("Cause should be StorageError", unknownError.throwable is StorageError.WriteError)
  }

  /** Test when Amber is installed */
  @Test
  fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {

    every { amberSignerClient.checkAmberInstalled() } returns true

    val result = authRepository.checkAmberInstalled()

    assertTrue("Should return true when Amber is installed", result)
  }

  /** Test when Amber is not installed */
  @Test
  fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {

    every { amberSignerClient.checkAmberInstalled() } returns false

    val result = authRepository.checkAmberInstalled()

    assertFalse("Should return false when Amber is not installed", result)
  }
}
