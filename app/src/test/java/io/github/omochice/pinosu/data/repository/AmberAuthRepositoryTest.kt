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

/*** AmberAuthRepositoryUnit tests for** Task 5.1: AuthRepositoryImplementation of* - getLoginState(), saveLoginState(), logout()oftest* - loginWithAmber()oftest (AmberInstalldetection)* - AmberResponseprocessing localsaveofflowtest** Task 5.3: AuthRepositoryUnit tests for ()* - Ambersuccess → localsavesuccessNormaltest ✓* - Amberfailureoferrortest ✓* - logoutprocessingtest ✓* - test ✓** Requirements: 1.3, 1.4, 1.5, 2.1, 2.2, 2.4, 2.5*/class AmberAuthRepositorytest {

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
/*** logged instateofgetsuccesstest** Task 5.1: getLoginState()implementation Requirement 2.2: login stateverify*/ @test
 fun testGetLoginState_WhenUserExists_ReturnsUser() = runtest {
// Given: LocalAuthDataSourceUser val expectedUser = User("npub1" + "a".repeat(59))
 coEvery { localAuthDataSource.getUser() } returns expectedUser

// When: getLoginState()call val result = authRepository.getLoginState()

// Then: User is returned assertEquals("Should return user from LocalAuthDataSource", expectedUser, result)
 coVerify { localAuthDataSource.getUser() }
 }

/*** not logged in stateofgettest** Task 5.1: getLoginState()implementation Requirement 2.2: login stateverify*/ @test
 fun testGetLoginState_WhenNoUser_ReturnsNull() = runtest {
// Given: LocalAuthDataSourcenull coEvery { localAuthDataSource.getUser() } returns null

// When: getLoginState()call val result = authRepository.getLoginState()

// Then: null is returned assertNull("Should return null when no user is stored", result)
 coVerify { localAuthDataSource.getUser() }
 }

// ========== saveLoginState() tests ==========
/*** login stateofsavesuccesstest** Task 5.1: saveLoginState()implementation Requirement 1.4: login statesave*/ @test
 fun testSaveLoginState_Success_ReturnsSuccess() = runtest {
// Given: LocalAuthDataSourcesuccessfullysave val user = User("npub1" + "a".repeat(59))
 coEvery { localAuthDataSource.saveUser(user) } returns Unit

// When: saveLoginState()call val result = authRepository.saveLoginState(user)

// Then: Success is returned assertTrue("Should return success", result.isSuccess)
 coVerify { localAuthDataSource.saveUser(user) }
 }

/*** login stateofsavefailuretest** Task 5.1: saveLoginState()implementation Requirement 5.2: error*/ @test
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
/*** logoutsuccesstest** Task 5.1: logout()implementation Requirement 2.4: logoutfunctionality*/ @test
 fun testLogout_Success_ReturnsSuccess() = runtest {
// Given: LocalAuthDataSourcesuccessfullyclear coEvery { localAuthDataSource.clearLoginState() } returns Unit

// When: logout()call val result = authRepository.logout()

// Then: Success is returned assertTrue("Should return success", result.isSuccess)
 coVerify { localAuthDataSource.clearLoginState() }
 }

/*** logoutfailuretest** Task 5.1: logout()implementation Requirement 5.2: error*/ @test
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
/*** Amberprocessingsuccesslocalsavesuccesstest** Task 5.1: AmberSignerClient → LocalAuthDataSourceflow Requirement 1.3, 1.4: Amberauthentication localsave*/ @test
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

/*** Amberedoftest** Task 5.1: Ambererror Requirement 1.5: error*/ @test
 fun testProcessAmberResponse_UserRejected_ReturnsLoginError() = runtest {
// Given: AmberUser val intent = Intent().apply { putExtra("rejected", true) }
 every { amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, intent) } returns
 Result.failure(io.github.omochice.pinosu.data.amber.AmberError.UserRejected)

// When: processAmberResponse()call val result = authRepository.processAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: UserRejectederroris returned assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue("Exception should be LoginError.UserRejected", exception is LoginError.UserRejected)
 }

// ========== Task 5.2: Additional Error H ling tests ==========
/*** AmberInstallerroroftest** Task 5.2: error Requirement 1.5: AmberNotInstalled*/ @test
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

/*** Ambererroroftest** Task 5.2: error Requirement 1.5: Timeout*/ @test
 fun testProcessAmberResponse_Timeout_ReturnsTimeoutError() = runtest {
// Given: AmberTimeouterror val intent = Intent()
 every { amberSignerClient.h leAmberResponse(any(), any()) } returns
 Result.failure(io.github.omochice.pinosu.data.amber.AmberError.Timeout)

// When: processAmberResponse()call val result = authRepository.processAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: LoginError.Timeoutis returned assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue("Exception should be LoginError.Timeout", exception is LoginError.Timeout)
 }

/*** Amber InvalidResponseerroroftest (NetworkError)** Task 5.2: error Requirement 5.2: NetworkError*/ @test
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

/*** test: Ambersuccess → localsavefailure** Task 5.2: Requirement 5.2: localsavefailureof*/ @test
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
/*** Amber is installedoftest** Task 5.1: AmberInstallverify Requirement 1.2: AmberInstalldetection*/ @test
 fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {
// Given: AmberSignerClienttrue every { amberSignerClient.checkAmberInstalled() } returns true

// When: Call checkAmberInstalled() val result = authRepository.checkAmberInstalled()

// Then: true is returned assertTrue("Should return true when Amber is installed", result)
 }

/*** Amber is not installedoftest** Task 5.1: AmberInstallverify Requirement 1.2: AmberInstalldetection*/ @test
 fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {
// Given: AmberSignerClientfalse every { amberSignerClient.checkAmberInstalled() } returns false

// When: Call checkAmberInstalled() val result = authRepository.checkAmberInstalled()

// Then: false is returned assertFalse("Should return false when Amber is not installed", result)
 }
}
