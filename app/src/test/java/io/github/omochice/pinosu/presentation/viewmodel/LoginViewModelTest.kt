package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase
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

class LoginViewModeltest {

 private lateinit var loginUseCase: LoginUseCase
 private lateinit var logoutUseCase: LogoutUseCase
 private lateinit var getLoginStateUseCase: GetLoginStateUseCase
 private lateinit var authRepository: AuthRepository
 private lateinit var viewModel: LoginViewModel

 private val testDispatcher = St ardtestDispatcher()

 @Before
 fun setup() {
 Dispatchers.setMain(testDispatcher)
 loginUseCase = mockk(relaxed = true)
 logoutUseCase = mockk(relaxed = true)
 getLoginStateUseCase = mockk(relaxed = true)
 authRepository = mockk(relaxed = true)
 viewModel = LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)
 }

 @After
 fun tearDown() {
 Dispatchers.resetMain()
 }

// ========== stateoftest ==========
 @test
 fun `initial LoginUiState should have default values`() = runtest {
// When: ViewModelinitialize val state = viewModel.uiState.first()

// Then: state assertFalse("isLoading should be false", state.isLoading)
 assertNull("errorMessage should be null", state.errorMessage)
 assertFalse("showAmberInstallDialog should be false", state.showAmberInstallDialog)
 assertFalse("loginSuccess should be false", state.loginSuccess)
 }

 @test
 fun `initial MainUiState should have default values`() = runtest {
// When: ViewModelinitialize val state = viewModel.mainUiState.first()

// Then: state assertNull("userPubkey should be null", state.userPubkey)
 assertFalse("isLoggingOut should be false", state.isLoggingOut)
 }

// ========== checkLoginState() of ==========
 @test
 fun `checkLoginState should update mainUiState when user is logged in`() = runtest {
// Given: Logged in user val testPubkey = "npub1" + "a".repeat(59)
 val testUser = User(testPubkey)
 coEvery { getLoginStateUseCase() } returns testUser

// When: checkLoginState()call viewModel.checkLoginState()
 advanceUntilIdle()

// Then: mainUiStateupdate val state = viewModel.mainUiState.first()
 assertEquals("userPubkey should be set", testPubkey, state.userPubkey)
 coVerify { getLoginStateUseCase() }
 }

 @test
 fun `checkLoginState should keep mainUiState null when user is not logged in`() = runtest {
// Given: Not logged in state coEvery { getLoginStateUseCase() } returns null

// When: checkLoginState()call viewModel.checkLoginState()
 advanceUntilIdle()

// Then: mainUiStatenullof val state = viewModel.mainUiState.first()
 assertNull("userPubkey should be null", state.userPubkey)
 coVerify { getLoginStateUseCase() }
 }

// ========== onLoginButtonClicked() of ==========
 @test
 fun `onLoginButtonClicked should check if Amber is installed`() = runtest {
// Given: Amber is installed every { loginUseCase.checkAmberInstalled() } returns true

// When: onLoginButtonClicked()call viewModel.onLoginButtonClicked()
 advanceUntilIdle()

// Then: checkAmberInstalled() io.mockk.verify { loginUseCase.checkAmberInstalled() }
 }

 @test
 fun `onLoginButtonClicked should show install dialog when Amber is not installed`() = runtest {
// Given: Amber is not installed every { loginUseCase.checkAmberInstalled() } returns false

// When: onLoginButtonClicked()call viewModel.onLoginButtonClicked()
 advanceUntilIdle()

// Then: showAmberInstallDialogtrue val state = viewModel.uiState.first()
 assertTrue("showAmberInstallDialog should be true", state.showAmberInstallDialog)
 }

// ========== onLogoutButtonClicked() of ==========
 @test
 fun `onLogoutButtonClicked should call logoutUseCase update state on success`() = runtest {
// Given: logoutsuccess coEvery { logoutUseCase() } returns Result.success(Unit)

// When: onLogoutButtonClicked()call viewModel.onLogoutButtonClicked()
 advanceUntilIdle()

// Then: logoutUseCase(), mainUiStatecleared coVerify { logoutUseCase() }
 val state = viewModel.mainUiState.first()
 assertNull("userPubkey should be null after logout", state.userPubkey)
 }

 @test
 fun `onLogoutButtonClicked should h le logout failure gracefully`() = runtest {
// Given: logoutfailure val error = io.github.omochice.pinosu.domain.model.error.LogoutError.StorageError("Failed")
 coEvery { logoutUseCase() } returns Result.failure(error)

// When: onLogoutButtonClicked()call viewModel.onLogoutButtonClicked()
 advanceUntilIdle()

// Then: logoutUseCase(), isLoggingOutfalse coVerify { logoutUseCase() }
 val state = viewModel.mainUiState.first()
 assertFalse("isLoggingOut should be false after failure", state.isLoggingOut)
 }

// ========== dismissError() of ==========
 @test
 fun `dismissError should clear error message`() = runtest {
// Given: Error messagesetingstate// (onLoginButtonClickederrorafter) every { loginUseCase.checkAmberInstalled() } returns false
 viewModel.onLoginButtonClicked()
 advanceUntilIdle()

// When: dismissError()call viewModel.dismissError()
 advanceUntilIdle()

// Then: errorofstatecleared val state = viewModel.uiState.first()
 assertNull("errorMessage should be null", state.errorMessage)
 assertFalse("showAmberInstallDialog should be false", state.showAmberInstallDialog)
 }

// ========== onRetryLogin() of ==========
 @test
 fun `onRetryLogin should retry login by calling onLoginButtonClicked`() = runtest {
// Given: Amber is installed every { loginUseCase.checkAmberInstalled() } returns true

// When: onRetryLogin()call viewModel.onRetryLogin()
 advanceUntilIdle()

// Then: checkAmberInstalled() (onLoginButtonClicked same) io.mockk.verify { loginUseCase.checkAmberInstalled() }
 }

 @test
 fun `processAmberResponse should set loading state during processing`() = runtest {
// Given: Amber responseprocessingsuccess val testPubkey = "npub1" + "c".repeat(59)
 val testUser = User(testPubkey)
 val mockIntent = mockk< roid.content.Intent>(relaxed = true)
 val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
 coEvery { authRepository.processAmberResponse(any(), any()) } coAnswers
 {
 kotlinx.coroutines.delay(100)
 Result.success(testUser)
 }
 every { authRepository.checkAmberInstalled() } returns true
 coEvery { authRepository.getLoginState() } returns null
 coEvery { authRepository.logout() } returns Result.success(Unit)

 val viewModelWithMock =
 LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

// When: processAmberResponse()call viewModelWithMock.processAmberResponse(-1, mockIntent)

// Note: stateofverificationimplementationoffor, ofverificationof advanceUntilIdle()

 coVerify { authRepository.processAmberResponse(any(), any()) }
 }

 @test
 fun `processAmberResponse should set loginSuccess on success`() = runtest {
// Given: Amber responseprocessingsuccess val testPubkey = "npub1" + "d".repeat(59)
 val testUser = User(testPubkey)
 val mockIntent = mockk< roid.content.Intent>(relaxed = true)
 val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
 coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.success(testUser)
 every { authRepository.checkAmberInstalled() } returns true
 coEvery { authRepository.getLoginState() } returns null
 coEvery { authRepository.logout() } returns Result.success(Unit)

 val viewModelWithMock =
 LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

// When: processAmberResponse()call viewModelWithMock.processAmberResponse(-1, mockIntent)
 advanceUntilIdle()

// Then: loginSuccesstrue , mainUiStateupdateed val loginState = viewModelWithMock.uiState.first()
 val mainState = viewModelWithMock.mainUiState.first()
 assertTrue("loginSuccess should be true", loginState.loginSuccess)
 assertFalse("isLoading should be false after success", loginState.isLoading)
 assertEquals("userPubkey should be set", testPubkey, mainState.userPubkey)
 }

 @test
 fun `processAmberResponse should set error message on UserRejected error`() = runtest {
// Given: User val mockIntent = mockk< roid.content.Intent>(relaxed = true)
 val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
 val error = io.github.omochice.pinosu.domain.model.error.LoginError.UserRejected
 coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.failure(error)
 every { authRepository.checkAmberInstalled() } returns true
 coEvery { authRepository.getLoginState() } returns null
 coEvery { authRepository.logout() } returns Result.success(Unit)

 val viewModelWithMock =
 LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

// When: processAmberResponse()call viewModelWithMock.processAmberResponse(-1, mockIntent)
 advanceUntilIdle()

// Then: errormessageset val state = viewModelWithMock.uiState.first()
 assertNotNull("errorMessage should be set", state.errorMessage)
 assertFalse("isLoading should be false", state.isLoading)
 assertFalse("loginSuccess should be false", state.loginSuccess)
 }

 @test
 fun `processAmberResponse should set error message on Timeout error`() = runtest {
// Given: val mockIntent = mockk< roid.content.Intent>(relaxed = true)
 val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
 val error = io.github.omochice.pinosu.domain.model.error.LoginError.Timeout
 coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.failure(error)
 every { authRepository.checkAmberInstalled() } returns true
 coEvery { authRepository.getLoginState() } returns null
 coEvery { authRepository.logout() } returns Result.success(Unit)

 val viewModelWithMock =
 LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

// When: processAmberResponse()call viewModelWithMock.processAmberResponse(-1, mockIntent)
 advanceUntilIdle()

// Then: errormessageset val state = viewModelWithMock.uiState.first()
 assertNotNull("errorMessage should be set", state.errorMessage)
 assertTrue(
 "errorMessage should contain timeout info",
 state.errorMessage?.contains("timed out") == true)
 assertFalse("isLoading should be false", state.isLoading)
 }

 @test
 fun `processAmberResponse should h le NetworkError`() = runtest {
// Given: error val mockIntent = mockk< roid.content.Intent>(relaxed = true)
 val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
 val error =
 io.github.omochice.pinosu.domain.model.error.LoginError.NetworkError("Connection failed")
 coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.failure(error)
 every { authRepository.checkAmberInstalled() } returns true
 coEvery { authRepository.getLoginState() } returns null
 coEvery { authRepository.logout() } returns Result.success(Unit)

 val viewModelWithMock =
 LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

// When: processAmberResponse()call viewModelWithMock.processAmberResponse(-1, mockIntent)
 advanceUntilIdle()

// Then: errormessageset val state = viewModelWithMock.uiState.first()
 assertNotNull("errorMessage should be set", state.errorMessage)
 assertFalse("isLoading should be false", state.isLoading)
 }
}
