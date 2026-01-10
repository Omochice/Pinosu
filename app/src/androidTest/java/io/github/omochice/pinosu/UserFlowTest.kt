package io.github.omochice.pinosu

import android.app.Activity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
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
 * 2. Amber not installed error flow
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
  fun loginFlow_step1_displaysLoginScreen() {

    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()
  }

  /** Note: This test verifies loading state before Amber Intent is launched */
  @Test
  fun loginFlow_step2_displaysLoadingOnButtonClick() {

    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns true
    every { mockNip55SignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()

    composeTestRule.onNodeWithText("Amberでログイン").assertIsNotEnabled()
  }

  /** Note: Amber Intent result simulation is required */
  @Test
  fun loginFlow_step3_navigatesToMainScreenOnSuccess() {

    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns true
    every { mockNip55SignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.saveUser(any()) } returns Unit

    every { mockNip55SignerClient.handleNip55Response(Activity.RESULT_OK, any()) } returns
        Result.success(
            io.github.omochice.pinosu.data.nip55.Nip55Response(
                pubkey = testUser.pubkey, packageName = "com.greenart7c3.nostrsigner"))

    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    // Note: Actual Intent result is handled in MainActivity's amberLauncher、
    // Here we verify UI transitions instead of ViewModel state changes directly、
    // verify UI transition results

    // This test verifies UI transitions only
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()
  }

  @Test
  fun amberNotInstalledFlow_step1_displaysErrorDialog() {

    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns false

    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    composeTestRule
        .onNodeWithText("Amberアプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertIsDisplayed()

    composeTestRule.onNodeWithText("インストール").assertIsDisplayed()

    composeTestRule.onNodeWithText("閉じる").assertIsDisplayed()
  }

  @Test
  fun amberNotInstalledFlow_step2_dismissDialog() {

    every { mockNip55SignerClient.checkNip55SignerInstalled() } returns false

    composeTestRule.onNodeWithText("Amberでログイン").performClick()
    composeTestRule
        .onNodeWithText("Amberアプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertIsDisplayed()

    composeTestRule.onNodeWithText("閉じる").performClick()

    composeTestRule
        .onNodeWithText("Amberアプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertDoesNotExist()

    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()
  }

  @Test
  fun logoutFlow_step1_navigatesToLoginScreenOnLogout() {

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
      composeTestRule.onAllNodesWithText("Amberでログイン").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()

    composeTestRule.onNodeWithText("ログアウト").assertDoesNotExist()
  }

  @Test
  fun appRestart_whenLoggedIn_displaysMainScreen() {

    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()

    val maskedPubkey = "1234abcd...5678efgh" // masking format first 8 chars...last 8 chars
    composeTestRule.onNodeWithText(maskedPubkey).assertIsDisplayed()

    composeTestRule.onNodeWithText("Amberでログイン").assertDoesNotExist()
  }

  @Test
  fun appRestart_whenNotLoggedIn_displaysLoginScreen() {

    coEvery { mockLocalAuthDataSource.getUser() } returns null

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Amberでログイン").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()

    composeTestRule.onNodeWithText("ログアウト").assertDoesNotExist()
  }
}
