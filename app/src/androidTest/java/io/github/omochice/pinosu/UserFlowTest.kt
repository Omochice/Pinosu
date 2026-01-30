package io.github.omochice.pinosu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
 * 1. Login flow (login screen → login button tap → loading display → main screen navigate)
 * 2. NIP-55 signer not installed error flow
 * 3. Logout flow (main screen → logout button tap → login screen navigate)
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

  @Before
  fun setup() {
    clearMocks(mockLocalAuthDataSource, mockNip55SignerClient, answers = false)
    coEvery { mockLocalAuthDataSource.getUser() } returns null
    hiltRule.inject()
  }

  @Test
  fun `login flow step1 should display login screen`() {
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()
  }

  @Test
  fun `Nip55Signer not installed flow step1 should display error dialog`() {
    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns false

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    composeTestRule.onNodeWithText("NIP-55対応アプリが必要です").assertIsDisplayed()

    composeTestRule.onNodeWithText("インストール").assertIsDisplayed()

    composeTestRule.onNodeWithText("閉じる").assertIsDisplayed()
  }

  @Test
  fun `Nip55Signer not installed flow step2 should dismiss dialog`() {
    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns false

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()
    composeTestRule.onNodeWithText("NIP-55対応アプリが必要です").assertIsDisplayed()

    composeTestRule.onNodeWithText("閉じる").performClick()

    composeTestRule.onNodeWithText("NIP-55対応アプリが必要です").assertDoesNotExist()

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()
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
      composeTestRule.onAllNodesWithText("ブックマーク").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマーク").assertIsDisplayed()

    composeTestRule.onNodeWithContentDescription("メニューを開く").performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("NIP-55対応アプリでログイン").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()
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
      composeTestRule.onAllNodesWithText("ブックマーク").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマーク").assertIsDisplayed()

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertDoesNotExist()
  }

  @Test
  fun `app restart when not logged in should display login screen`() {
    coEvery { mockLocalAuthDataSource.getUser() } returns null

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("NIP-55対応アプリでログイン").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()

    composeTestRule.onNodeWithText("ブックマーク").assertDoesNotExist()
  }
}
