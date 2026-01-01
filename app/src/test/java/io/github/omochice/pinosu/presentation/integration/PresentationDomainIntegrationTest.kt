package io.github.omochice.pinosu.presentation.integration

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.LoginError
import io.github.omochice.pinosu.domain.model.error.LogoutError
import io.github.omochice.pinosu.domain.usecase.AmberGetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.AmberLoginUseCase
import io.github.omochice.pinosu.domain.usecase.AmberLogoutUseCase
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase
import io.github.omochice.pinosu.presentation.viewmodel.LoginViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.St ardtestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runtest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*import org.junit.Before
import org.junit.test

class PresentationDomainIntegrationtest {

 private lateinit var authRepository: AuthRepository
 private lateinit var loginUseCase: LoginUseCase
 private lateinit var logoutUseCase: LogoutUseCase
 private lateinit var getLoginStateUseCase: GetLoginStateUseCase
 private lateinit var viewModel: LoginViewModel

 private val testDispatcher = St ardtestDispatcher()

 @Before
 fun setup() {
 Dispatchers.setMain(testDispatcher)

// Data authRepository = mockk(relaxed = true)

// DomainwhenImplementation of (test) loginUseCase = AmberLoginUseCase(authRepository)
 logoutUseCase = AmberLogoutUseCase(authRepository)
 getLoginStateUseCase = AmberGetLoginStateUseCase(authRepository)

// PresentationwhenImplementation of viewModel = LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)
 }

 @After
 fun tearDown() {
 Dispatchers.resetMain()
 }

// ========== LoginViewModel + UseCasestest ==========
/**
 * Login button tap triggers Amber installation check and shows dialog if not installed
 *
 * flow:
 * 1. User taps Login button (calls LoginViewModel.onLoginButtonClicked)
 * 2. LoginUseCase checks Amber installation (via LoginUseCase.checkAmberInstalled)
 * 3. AuthRepository queries AmberSignerClient for installation status
 * 4. ViewModel updates UI state (showAmberInstallDialog = true)
 */
@test
 fun `login flow - when Amber not installed - should show install dialog`() = runtest {
// Given: Amber is not installed every { authRepository.checkAmberInstalled() } returns false

// When: Tap login button viewModel.onLoginButtonClicked()
 advanceUntilIdle()

// Then: AmberInstalldialogis displayed val state = viewModel.uiState.first()
 assertTrue("showAmberInstallDialog should be true", state.showAmberInstallDialog)
 assertFalse("isLoading should be false", state.isLoading)
 assertNull("errorMessage should be null", state.errorMessage)

// AuthRepositoryofcheckAmberInstalled()Verify that io.mockk.verify { authRepository.checkAmberInstalled() }
 }

/**
 * login succeedsflow → UIstateupdate → Main screendisplay
 *
 * flow:
 * 1. Amber response is processed (via LoginViewModel.processAmberResponse)
 * 2. AuthRepository processes the response
 * 3. User data is saved successfully
 * 4. ViewModel updates UI state (loginSuccess = true, userPubkey is set)
 */
@test
 fun `login flow - when Amber response success - should update UI state navigate to main`() =
 runtest {
// Given: Amber responseprocessingsuccess val testPubkey = "npub1" + "a".repeat(59)
 val testUser = User(testPubkey)
 val mockIntent = mockk< roid.content.Intent>(relaxed = true)
 coEvery { authRepository.processAmberResponse(any(), any()) } returns
 Result.success(testUser)

// When: Amberprocessing viewModel.processAmberResponse(-1, mockIntent)
 advanceUntilIdle()

// Then: login succeedsstate , Userseted val loginState = viewModel.uiState.first()
 val mainState = viewModel.mainUiState.first()

 assertTrue("loginSuccess should be true", loginState.loginSuccess)
 assertFalse("isLoading should be false", loginState.isLoading)
 assertNull("errorMessage should be null", loginState.errorMessage)
 assertEquals("userPubkey should be set", testPubkey, mainState.userPubkey)

// AuthRepositoryofprocessAmberResponse()Verify that coVerify { authRepository.processAmberResponse(any(), any()) }
 }

/**
 * app startsoflogin stateverify → logged in → Main screendisplay
 *
 * flow:
 * 1. App checks login state at startup (via LoginViewModel.checkLoginState)
 * 2. GetLoginStateUseCase retrieves login state
 * 3. AuthRepository fetches stored user data
 * 4. ViewModel updates Main screen UI state (userPubkey is set)
 */
@test
 fun `startup flow - when user logged in - should restore login state`() = runtest {
// Given: Logged in usersaveing val testPubkey = "npub1" + "b".repeat(59)
 val testUser = User(testPubkey)
 coEvery { authRepository.getLoginState() } returns testUser

// When: Check login state on app startup viewModel.checkLoginState()
 advanceUntilIdle()

// Then: logged instateed val state = viewModel.mainUiState.first()
 assertEquals("userPubkey should be restored", testPubkey, state.userPubkey)

// AuthRepositoryofgetLoginState()Verify that coVerify { authRepository.getLoginState() }
 }

/**
 * app startsoflogin stateverify → not logged in → Login screendisplay
 *
 * flow:
 * 1. App checks login state at startup (via LoginViewModel.checkLoginState)
 * 2. GetLoginStateUseCase retrieves login state
 * 3. AuthRepository returns null (user not logged in)
 * 4. ViewModel Main screen UI state remains empty (userPubkey = null)
 */
@test
 fun `startup flow - when user not logged in - should keep null state`() = runtest {
// Given: Not logged in state coEvery { authRepository.getLoginState() } returns null

// When: Check login state on app startup viewModel.checkLoginState()
 advanceUntilIdle()

// Then: login statenullof val state = viewModel.mainUiState.first()
 assertNull("userPubkey should be null", state.userPubkey)

// AuthRepositoryofgetLoginState()Verify that coVerify { authRepository.getLoginState() }
 }

// ========== errorflowtest ==========
/**
 * User rejection during Amber flow shows error message and allows retry
 *
 * flow:
 * 1. Amber response is processed (via LoginViewModel.processAmberResponse)
 * 2. AuthRepository detects user rejected the request
 * 3. LoginError.UserRejected error is returned
 * 4. ViewModel sets error message in UI state
 * 5. User can tap retry button (onRetryLogin)
 */
@test
 fun `error flow - when user rejected - should show error allow retry`() = runtest {
// Given: UserAmber val mockIntent = mockk< roid.content.Intent>(relaxed = true)
 val error = LoginError.UserRejected
 coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.failure(error)
 every { authRepository.checkAmberInstalled() } returns true

// When: Amberprocessing viewModel.processAmberResponse(-1, mockIntent)
 advanceUntilIdle()

// Then: errormessageis displayed val stateAfterError = viewModel.uiState.first()
 assertNotNull("errorMessage should be set", stateAfterError.errorMessage)
 assertFalse("loginSuccess should be false", stateAfterError.loginSuccess)
 assertFalse("isLoading should be false", stateAfterError.isLoading)

// possibleaVerify that viewModel.onRetryLogin()
 advanceUntilIdle()

// Amberverifyed io.mockk.verify(atLeast = 1) { authRepository.checkAmberInstalled() }
 }

/**
 * error → messagedisplay → possible
 *
 * flow:
 * 1. Amber response is processed (via LoginViewModel.processAmberResponse)
 * 2. AuthRepository detects an error
 * 3. LoginError.Timeout error is returned
 * 4. ViewModel sets timeout message in UI state
 */
@test
 fun `error flow - when timeout - should show timeout error message`() = runtest {
// Given: Amberprocessing val mockIntent = mockk< roid.content.Intent>(relaxed = true)
 val error = LoginError.Timeout
 coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.failure(error)

// When: Amberprocessing viewModel.processAmberResponse(-1, mockIntent)
 advanceUntilIdle()

// Then: errormessageis displayed val state = viewModel.uiState.first()
 assertNotNull("errorMessage should be set", state.errorMessage)
 assertTrue(
 "errorMessage should contain timeout info",
 state.errorMessage?.contains("timed out") == true)
 assertFalse("loginSuccess should be false", state.loginSuccess)
 assertFalse("isLoading should be false", state.isLoading)
 }

/**
 * error → errormessagedisplay
 *
 * flow:
 * 1. Amber response is processed (via LoginViewModel.processAmberResponse)
 * 2. AuthRepository detects an error
 * 3. LoginError.NetworkError error is returned
 * 4. ViewModel sets network error message in UI state
 */
@test
 fun `error flow - when network error - should show network error message`() = runtest {
// Given: error val mockIntent = mockk< roid.content.Intent>(relaxed = true)
 val error = LoginError.NetworkError("Connection failed")
 coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.failure(error)

// When: Amberprocessing viewModel.processAmberResponse(-1, mockIntent)
 advanceUntilIdle()

// Then: errormessageis displayed val state = viewModel.uiState.first()
 assertNotNull("errorMessage should be set", state.errorMessage)
 assertFalse("loginSuccess should be false", state.loginSuccess)
 assertFalse("isLoading should be false", state.isLoading)
 }

/**
 * Error dialogClose → errorstateclear
 *
 * flow:
 * 1. Error dialog is displayed
 * 2. User dismisses the error dialog (via dismissError)
 * 3. ViewModel clears error state
 */
@test
 fun `error flow - when error dismissed - should clear error state`() = runtest {
// Given: AmberInstallerroris displayeding every { authRepository.checkAmberInstalled() } returns false
 viewModel.onLoginButtonClicked()
 advanceUntilIdle()

 val stateBeforeDismiss = viewModel.uiState.first()
 assertTrue("showAmberInstallDialog should be true", stateBeforeDismiss.showAmberInstallDialog)

// When: Error dialog viewModel.dismissError()
 advanceUntilIdle()

// Then: errorstateclear val stateAfterDismiss = viewModel.uiState.first()
 assertNull("errorMessage should be null", stateAfterDismiss.errorMessage)
 assertFalse("showAmberInstallDialog should be false", stateAfterDismiss.showAmberInstallDialog)
 }

// ========== logoutflowtest ==========
/**
 * Successful logout clears login state and updates UI
 *
 * flow:
 * 1. User taps Logout button (via LoginViewModel.onLogoutButtonClicked)
 * 2. LogoutUseCase performs logout processing
 * 3. AuthRepository successfully clears login state
 * 4. ViewModel updates UI state (userPubkey = null)
 */
@test
 fun `logout flow - when logout success - should clear login state`() = runtest {
// Given: Logged in state val testPubkey = "npub1" + "c".repeat(59)
 val testUser = User(testPubkey)
 coEvery { authRepository.getLoginState() } returns testUser
 viewModel.checkLoginState()
 advanceUntilIdle()

 val stateBeforeLogout = viewModel.mainUiState.first()
 assertEquals("userPubkey should be set", testPubkey, stateBeforeLogout.userPubkey)

// logoutprocessingsuccessset coEvery { authRepository.logout() } returns Result.success(Unit)

// When: Tap logout button viewModel.onLogoutButtonClicked()
 advanceUntilIdle()

// Then: login stateclear val stateAfterLogout = viewModel.mainUiState.first()
 assertNull("userPubkey should be null after logout", stateAfterLogout.userPubkey)
 assertFalse("isLoggingOut should be false", stateAfterLogout.isLoggingOut)

// AuthRepositoryoflogout()Verify that coVerify { authRepository.logout() }
 }

/**
 * logoutfailureflow → error → login state
 *
 * flow:
 * 1. User taps Logout button (via LoginViewModel.onLogoutButtonClicked)
 * 2. LogoutUseCase attempts logout processing
 * 3. AuthRepository encounters an error
 * 4. ViewModel handles error gracefully (isLoggingOut = false)
 */
@test
 fun `logout flow - when logout fails - should h le error gracefully`() = runtest {
// Given: Logged in statelogoutfailure val testPubkey = "npub1" + "d".repeat(59)
 val testUser = User(testPubkey)
 coEvery { authRepository.getLoginState() } returns testUser
 viewModel.checkLoginState()
 advanceUntilIdle()

 val error = LogoutError.StorageError("Failed to clear storage")
 coEvery { authRepository.logout() } returns Result.failure(error)

// When: Tap logout button viewModel.onLogoutButtonClicked()
 advanceUntilIdle()

// Then: errored, isLoggingOutfalse val state = viewModel.mainUiState.first()
 assertFalse("isLoggingOut should be false after error", state.isLoggingOut)

// AuthRepositoryoflogout()Verify that coVerify { authRepository.logout() }
 }
}
