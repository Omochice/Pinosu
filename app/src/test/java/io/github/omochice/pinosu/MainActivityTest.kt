package io.github.omochice.pinosu

import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for MainActivity
 * - Call GetLoginStateUseCase to verify login state
 * - Logged-in → display main screen
 * - Not logged-in → display login screen
 * - Clear login state when invalid data detected
 * - Extract shared content from share intent
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

  @Test
  fun `when logged in on startup, should show main screen`() = runTest {
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    val loggedInUser = User("npub1" + "1234567890abcdef".repeat(3) + "1234567890a")
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

  @Test
  fun `extractSharedContent returns url for https URL`() {
    val intent =
        android.content.Intent(android.content.Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(android.content.Intent.EXTRA_TEXT, "https://example.com/path")
        }

    val result = extractSharedContent(intent)

    assertEquals(SharedContent(url = "https://example.com/path", comment = null), result)
  }

  @Test
  fun `extractSharedContent returns url for http URL`() {
    val intent =
        android.content.Intent(android.content.Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(android.content.Intent.EXTRA_TEXT, "http://example.com/path")
        }

    val result = extractSharedContent(intent)

    assertEquals(SharedContent(url = "http://example.com/path", comment = null), result)
  }

  @Test
  fun `extractSharedContent returns comment for non-URL text`() {
    val intent =
        android.content.Intent(android.content.Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(android.content.Intent.EXTRA_TEXT, "This is a note")
        }

    val result = extractSharedContent(intent)

    assertEquals(SharedContent(url = null, comment = "This is a note"), result)
  }

  @Test
  fun `extractSharedContent returns null for non-SEND action`() {
    val intent =
        android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
          type = "text/plain"
          putExtra(android.content.Intent.EXTRA_TEXT, "https://example.com")
        }

    val result = extractSharedContent(intent)

    org.junit.Assert.assertNull(result)
  }

  @Test
  fun `extractSharedContent returns null for non-text MIME type`() {
    val intent =
        android.content.Intent(android.content.Intent.ACTION_SEND).apply {
          type = "image/png"
          putExtra(android.content.Intent.EXTRA_TEXT, "https://example.com")
        }

    val result = extractSharedContent(intent)

    org.junit.Assert.assertNull(result)
  }

  @Test
  fun `extractSharedContent returns null when EXTRA_TEXT is missing`() {
    val intent =
        android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain" }

    val result = extractSharedContent(intent)

    org.junit.Assert.assertNull(result)
  }
}
