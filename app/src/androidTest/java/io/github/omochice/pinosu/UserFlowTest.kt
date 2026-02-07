package io.github.omochice.pinosu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test scenarios:
 * 1. Login flow (login screen -> login button tap -> loading display -> main screen navigate)
 * 2. NIP-55 signer not installed error flow
 * 3. Logout flow (main screen -> logout button tap -> login screen navigate)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UserFlowTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @BindValue @JvmField val mockNip55SignerClient: Nip55SignerClient = mockk(relaxed = true)

  @BindValue
  @JvmField
  val mockLocalAuthDataSource: LocalAuthDataSource =
      mockk(relaxed = true) { coEvery { getUser() } returns null }

  private val context = InstrumentationRegistry.getInstrumentation().targetContext

  @Before
  fun setup() {
    clearMocks(mockLocalAuthDataSource, mockNip55SignerClient, answers = false)
    coEvery { mockLocalAuthDataSource.getUser() } returns null
    hiltRule.inject()
  }

  @Test
  fun `login flow step1 should display login screen`() {
    composeTestRule
        .onNodeWithText(context.getString(R.string.button_login_with_nip55))
        .assertIsDisplayed()
  }

  @Test
  fun `Nip55Signer not installed flow step1 should display error dialog`() {
    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns false

    composeTestRule
        .onNodeWithText(context.getString(R.string.button_login_with_nip55))
        .performClick()

    composeTestRule
        .onNodeWithText(context.getString(R.string.dialog_title_nip55_signer_required))
        .assertIsDisplayed()

    composeTestRule.onNodeWithText(context.getString(R.string.button_install)).assertIsDisplayed()

    composeTestRule.onNodeWithText(context.getString(R.string.button_close)).assertIsDisplayed()
  }

  @Test
  fun `Nip55Signer not installed flow step2 should dismiss dialog`() {
    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns false

    composeTestRule
        .onNodeWithText(context.getString(R.string.button_login_with_nip55))
        .performClick()
    composeTestRule
        .onNodeWithText(context.getString(R.string.dialog_title_nip55_signer_required))
        .assertIsDisplayed()

    composeTestRule.onNodeWithText(context.getString(R.string.button_close)).performClick()

    composeTestRule
        .onNodeWithText(context.getString(R.string.dialog_title_nip55_signer_required))
        .assertDoesNotExist()

    composeTestRule
        .onNodeWithText(context.getString(R.string.button_login_with_nip55))
        .assertIsDisplayed()
  }

  @Test
  fun `logout flow step1 should navigate to login screen on logout`() {
    val testUser =
        User(
            pubkey =
                requireNotNull(
                    Pubkey.parse("npub1" + "1234567890abcdef".repeat(3) + "1234567890a")))
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser
    coEvery { mockLocalAuthDataSource.clearLoginState() } returns Unit

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithText(context.getString(R.string.title_bookmarks))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText(context.getString(R.string.title_bookmarks)).assertIsDisplayed()

    composeTestRule
        .onNodeWithContentDescription(context.getString(R.string.cd_open_menu))
        .performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodesWithText(context.getString(R.string.menu_logout))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText(context.getString(R.string.menu_logout)).performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodesWithText(context.getString(R.string.button_login_with_nip55))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule
        .onNodeWithText(context.getString(R.string.button_login_with_nip55))
        .assertIsDisplayed()
  }

  @Test
  fun `app restart when logged in should display bookmark screen`() {
    val testUser =
        User(
            pubkey =
                requireNotNull(
                    Pubkey.parse("npub1" + "1234567890abcdef".repeat(3) + "1234567890a")))
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithText(context.getString(R.string.title_bookmarks))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithText(context.getString(R.string.title_bookmarks)).assertIsDisplayed()

    composeTestRule
        .onNodeWithText(context.getString(R.string.button_login_with_nip55))
        .assertDoesNotExist()
  }

  @Test
  fun `app restart when not logged in should display login screen`() {
    coEvery { mockLocalAuthDataSource.getUser() } returns null

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithText(context.getString(R.string.button_login_with_nip55))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule
        .onNodeWithText(context.getString(R.string.button_login_with_nip55))
        .assertIsDisplayed()

    composeTestRule.onNodeWithText(context.getString(R.string.title_bookmarks)).assertDoesNotExist()
  }
}
