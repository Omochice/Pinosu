package io.github.omochice.pinosu.data.repository

import roid.content.Intent
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
import kotlinx.coroutines.test.runtest
import org.junit.Assert.*import org.junit.Before
import org.junit.test


 private lateinit var amberSignerClient: AmberSignerClient
 private lateinit var localAuthDataSource: LocalAuthDataSource
 private lateinit var authRepository: AuthRepository

 @Before
 fun setup() {
 amberSignerClient = mockk(relaxed = true)
 localAuthDataSource = mockk(relaxed = true)
 authRepository = AmberAuthRepository(amberSignerClient, localAuthDataSource)
 }

// ========== getLoginState() tests ==========
 fun testGetLoginState_WhenUserExists_ReturnsUser() = runtest {
// Given: LocalAuthDataSourceUser val expectedUser = User("npub1" + "a".repeat(59))
 coEvery { localAuthDataSource.getUser() } returns expectedUser

// When: getLoginState()call val result = authRepository.getLoginState()

// Then: User is returned assertEquals("Should return user from LocalAuthDataSource", expectedUser, result)
 coVerify { localAuthDataSource.getUser() }
 }

 fun testGetLoginState_WhenNoUser_ReturnsNull() = runtest {
// Given: LocalAuthDataSourcenull coEvery { localAuthDataSource.getUser() } returns null

// When: getLoginState()call val result = authRepository.getLoginState()

// Then: null is returned assertNull("Should return null when no user is stored", result)
 coVerify { localAuthDataSource.getUser() }
 }

// ========== saveLoginState() tests ==========
 fun testSaveLoginState_Success_ReturnsSuccess() = runtest {
// Given: LocalAuthDataSourcesuccessfullysave val user = User("npub1" + "a".repeat(59))
 coEvery { localAuthDataSource.saveUser(user) } returns Unit

// When: saveLoginState()call val result = authRepository.saveLoginState(user)

// Then: Success is returned assertTrue("Should return success", result.isSuccess)
 coVerify { localAuthDataSource.saveUser(user) }
 }

 fun testSaveLoginState_Failure_ReturnsStorageError() = runtest {
// Given: LocalAuthDataSourcethrow exception val user = User("npub1" + "a".repeat(59))
 val storageError = StorageError.WriteError("Failed to save")
 coEvery { localAuthDataSource.saveUser(user) } throws storageError

// When: saveLoginState()call val result = authRepository.saveLoginState(user)

// Then: Failure is returned assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue("Exception should be StorageError", exception is StorageError.WriteError)
 coVerify { localAuthDataSource.saveUser(user) }
 }

// ========== logout() tests ==========
 fun testLogout_Success_ReturnsSuccess() = runtest {
// Given: LocalAuthDataSourcesuccessfullyclear coEvery { localAuthDataSource.clearLoginState() } returns Unit

// When: logout()call val result = authRepository.logout()

// Then: Success is returned assertTrue("Should return success", result.isSuccess)
 coVerify { localAuthDataSource.clearLoginState() }
 }

 fun testLogout_Failure_ReturnsLogoutError() = runtest {
// Given: LocalAuthDataSourcethrow exception val storageError = StorageError.WriteError("Failed to clear")
 coEvery { localAuthDataSource.clearLoginState() } throws storageError

// When: logout()call val result = authRepository.logout()

// Then: Failure is returned assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue(
 "Exception should be LogoutError.StorageError", exception is LogoutError.StorageError)
 coVerify { localAuthDataSource.clearLoginState() }
 }

// ========== processAmberResponse() tests ==========
 fun testProcessAmberResponse_Success_SavesUserAndReturnsSuccess() = runtest {
// Given: Ambersuccess, localsavesuccess val pubkey = "npub1" + "a".repeat(59)
 val intent = Intent().apply { putExtra("result", pubkey) }
 val amberResponse = AmberResponse(pubkey, AmberSignerClient.AMBER_PACKAGE_NAME)

 every { amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, intent) } returns
 Result.success(amberResponse)
 coEvery { localAuthDataSource.saveUser(any()) } returns Unit

// When: processAmberResponse()call val result = authRepository.processAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: Successed, Usersaveed assertTrue("Should return success", result.isSuccess)
 val user = result.getOrNull()
 assertNotNull("User should not be null", user)
 assertEquals("Pubkey should match", pubkey, user?.pubkey)
 coVerify { localAuthDataSource.saveUser(any()) }
 }

 fun testProcessAmberResponse_UserRejected_ReturnsLoginError() = runtest {
// Given: AmberUser val intent = Intent().apply { putExtra("rejected", true) }
 every { amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, intent) } returns
 Result.failure(io.github.omochice.pinosu.data.amber.AmberError.UserRejected)

// When: processAmberResponse()call val result = authRepository.processAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: UserRejectederroris returned assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue("Exception should be LoginError.UserRejected", exception is LoginError.UserRejected)
 }

 fun testProcessAmberResponse_AmberNotInstalled_ReturnsAmberNotInstalledError() = runtest {
// Given: AmberNotInstallederror val intent = Intent()
 every { amberSignerClient.h leAmberResponse(any(), any()) } returns
 Result.failure(io.github.omochice.pinosu.data.amber.AmberError.NotInstalled)

// When: processAmberResponse()call val result = authRepository.processAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: LoginError.AmberNotInstalledis returned assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue(
 "Exception should be LoginError.AmberNotInstalled",
 exception is LoginError.AmberNotInstalled)
 }

 fun testProcessAmberResponse_Timeout_ReturnsTimeoutError() = runtest {
// Given: AmberTimeouterror val intent = Intent()
 every { amberSignerClient.h leAmberResponse(any(), any()) } returns
 Result.failure(io.github.omochice.pinosu.data.amber.AmberError.Timeout)

// When: processAmberResponse()call val result = authRepository.processAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: LoginError.Timeoutis returned assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue("Exception should be LoginError.Timeout", exception is LoginError.Timeout)
 }

 fun testProcessAmberResponse_InvalidResponse_ReturnsNetworkError() = runtest {
// Given: AmberInvalidResponseerror val intent = Intent()
 every { amberSignerClient.h leAmberResponse(any(), any()) } returns
 Result.failure(
 io.github.omochice.pinosu.data.amber.AmberError.InvalidResponse("Invalid data"))

// When: processAmberResponse()call val result = authRepository.processAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: LoginError.NetworkErroris returned assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue("Exception should be LoginError.NetworkError", exception is LoginError.NetworkError)
 }

 fun testProcessAmberResponse_AmberSuccess_LocalStorageFail_ReturnsUnknownError() = runtest {
// Given: Ambersuccesslocalsavefailure val pubkey = "npub1" + "a".repeat(59)
 val intent = Intent().apply { putExtra("result", pubkey) }
 val amberResponse = AmberResponse(pubkey, AmberSignerClient.AMBER_PACKAGE_NAME)

 every { amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, intent) } returns
 Result.success(amberResponse)
 coEvery { localAuthDataSource.saveUser(any()) } throws StorageError.WriteError("Storage full")

// When: processAmberResponse()call val result = authRepository.processAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: LoginError.UnknownErroris returned (StorageError) assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue("Exception should be LoginError.UnknownError", exception is LoginError.UnknownError)

// StorageError includedVerify that val unknownError = exception as LoginError.UnknownError
 assertTrue("Cause should be StorageError", unknownError.throwable is StorageError.WriteError)
 }

// ========== checkAmberInstalled() tests ==========
 fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {
// Given: AmberSignerClienttrue every { amberSignerClient.checkAmberInstalled() } returns true

// When: Call checkAmberInstalled() val result = authRepository.checkAmberInstalled()

// Then: true is returned assertTrue("Should return true when Amber is installed", result)
 }

 fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {
// Given: AmberSignerClientfalse every { amberSignerClient.checkAmberInstalled() } returns false

// When: Call checkAmberInstalled() val result = authRepository.checkAmberInstalled()

// Then: false is returned assertFalse("Should return false when Amber is not installed", result)
 }
}
