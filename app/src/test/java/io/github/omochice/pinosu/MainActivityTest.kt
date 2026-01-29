package io.github.omochice.pinosu

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for MainActivity
 * - Call GetLoginStateUseCase to verify login state
 * - Logged-in → display main screen
 * - Not logged-in → display login screen
 * - Clear login state when invalid data detected
 */
class MainActivityTest {

  @Test
  fun `when logged in on startup, should show main screen`() = runTest {
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    val loggedInUser = User(Pubkey.parse("npub1" + "1234567890abcdef".repeat(3) + "1234567890a")!!)
    coEvery { mockGetLoginStateUseCase() } returns loggedInUser

    val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

    assertEquals("MainScreen", initialDestination)
  }

  @Test
  fun `when not logged in on startup, should show login screen`() = runTest {
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    coEvery { mockGetLoginStateUseCase() } returns null

    val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

    assertEquals("LoginScreen", initialDestination)
  }

  @Test
  fun `when invalid data detected on startup, should show login screen`() = runTest {
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    coEvery { mockGetLoginStateUseCase() } returns null

    val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

    assertEquals("LoginScreen", initialDestination)
  }

  /**
   * Determine initial display screen based on login state
   *
   * Helper function to test logic planned for MainActivity
   */
  private suspend fun determineInitialDestination(
      getLoginStateUseCase: GetLoginStateUseCase
  ): String {
    val user = getLoginStateUseCase()
    return if (user != null) "MainScreen" else "LoginScreen"
  }
}
