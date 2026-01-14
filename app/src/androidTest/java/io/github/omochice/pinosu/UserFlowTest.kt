package io.github.omochice.pinosu

import android.app.Activity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.domain.model.User
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

  @BindValue @JvmField val mockLocalAuthDataSource: LocalAuthDataSource = mockk(relaxed = true)

  @Before
  fun setup() {
    hiltRule.inject()
    coEvery { mockLocalAuthDataSource.getUser() } returns null
  }

  @Test
  fun `login flow step1 should display login screen`() {
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()
  }

  @Test
  fun `login flow step2 should display loading on button click`() {
    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns true
    every { mockNip55SignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsNotEnabled()
  }

  @Test
  fun `login flow step3 should navigate to main screen on success`() {
    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns true
    every { mockNip55SignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.saveUser(any()) } returns Unit

    every { mockNip55SignerClient.handleNip55Response(Activity.RESULT_OK, any()) } returns
        Result.success(
            io.github.omochice.pinosu.data.nip55.Nip55Response(
                pubkey = testUser.pubkey, packageName = "com.greenart7c3.nostrsigner"))

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()
  }

  @Test
  fun `Nip55Signer not installed flow step1 should display error dialog`() {
    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns false

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    composeTestRule
        .onNodeWithText("NIP-55対応アプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertIsDisplayed()

    composeTestRule.onNodeWithText("インストール").assertIsDisplayed()

    composeTestRule.onNodeWithText("閉じる").assertIsDisplayed()
  }

  @Test
  fun `Nip55Signer not installed flow step2 should dismiss dialog`() {
    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns false

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()
    composeTestRule
        .onNodeWithText("NIP-55対応アプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertIsDisplayed()

    composeTestRule.onNodeWithText("閉じる").performClick()

    composeTestRule
        .onNodeWithText("NIP-55対応アプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertDoesNotExist()

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()
  }

  @Test
  fun `logout flow step1 should navigate to login screen on logout`() {
    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser
    coEvery { mockLocalAuthDataSource.clearLoginState() } returns Unit

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()

    composeTestRule.onNodeWithText("ログアウト").performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("NIP-55対応アプリでログイン").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()

    composeTestRule.onNodeWithText("ログアウト").assertDoesNotExist()
  }

  @Test
  fun `app restart when logged in should display main screen`() {
    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()

    val maskedPubkey = "${testUser.pubkey.take(8)}...${testUser.pubkey.takeLast(8)}"
    composeTestRule.onNodeWithText(maskedPubkey).assertIsDisplayed()

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertDoesNotExist()
  }

  @Test
  fun `app restart when not logged in should display login screen`() {
    coEvery { mockLocalAuthDataSource.getUser() } returns null

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("NIP-55対応アプリでログイン").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()

    composeTestRule.onNodeWithText("ログアウト").assertDoesNotExist()
  }
}
