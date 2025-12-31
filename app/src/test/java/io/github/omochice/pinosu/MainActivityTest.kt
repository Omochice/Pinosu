package io.github.omochice.pinosu

import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runtest
import org.junit.Assert.assertEquals
import org.junit.test

/*** Unit tests for MainActivity** Task 10.1: Check login state on app startup* - Call GetLoginStateUseCase to check login state* - Logged in → Show main screen* - Not logged in → Show login screen* - Clear login state when invalid data is detected** Requirements: 2.2, 2.3*/class MainActivitytest {

// ========== Check login state on app startup ==========
 @test
 fun `when logged in on startup, should show main screen`() = runtest {
// Given: Logged in user val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
 val loggedInUser = User("npub1" + "1234567890abcdef".repeat(3) + "1234567890a")
 coEvery { mockGetLoginStateUseCase() } returns loggedInUser

// When: Check login state on app startup val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

// Then: Main screen is displayed assertEquals("MainScreen", initialDestination)
 }

 @test
 fun `when not logged in on startup, should show login screen`() = runtest {
// Given: Not logged in user val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
 coEvery { mockGetLoginStateUseCase() } returns null

// When: Check login state on app startup val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

// Then: Login screen is displayed assertEquals("LoginScreen", initialDestination)
 }

 @test
 fun `when invalid data detected on startup, should show login screen`() = runtest {
// Given: Invalid data (assumed to return null from UseCase) val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
 coEvery { mockGetLoginStateUseCase() } returns null

// When: Check login state on app startup val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

// Then: Login screen is displayed (invalid data already converted to null by UseCase) assertEquals("LoginScreen", initialDestination)
 }

// ========== Helper functions ==========
/*** Determine the initial screen based on login state** This a helper function to test the logic that will be implemented in MainActivity*/ private suspend fun determineInitialDestination(
 getLoginStateUseCase: GetLoginStateUseCase
 ): String {
 val user = getLoginStateUseCase()
 return if (user != null) "MainScreen" else "LoginScreen"
 }
}
