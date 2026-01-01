package io.github.omochice.pinosu

import android.app.Activity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.omochice.pinosu.data.amber.AmberSignerClient
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
 * テスト内容:
 * 1. ログインフロー（ログイン画面 → ログインボタンタップ → ローディング表示 → メイン画面遷移）
 * 2. Amber未インストールエラーフロー
 * 3. ログアウトフロー（メイン画面 → ログアウトボタンタップ → ログイン画面遷移）
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UserFlowTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @BindValue @JvmField val mockAmberSignerClient: AmberSignerClient = mockk(relaxed = true)

  @BindValue @JvmField val mockLocalAuthDataSource: LocalAuthDataSource = mockk(relaxed = true)

  @Before
  fun setup() {
    hiltRule.inject()
    // デフォルトで未ログイン状態
    coEvery { mockLocalAuthDataSource.getUser() } returns null
  }

  @Test
  fun loginFlow_step1_displaysLoginScreen() {

    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()
  }

  /** Note: このテストはAmber Intentが起動される前のローディング状態を確認する */
  @Test
  fun loginFlow_step2_displaysLoadingOnButtonClick() {

    every { mockAmberSignerClient.checkAmberInstalled() } returns true
    every { mockAmberSignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()

    composeTestRule.onNodeWithText("Amberでログイン").assertIsNotEnabled()
  }

  /** Note: Amber Intent結果のシミュレーションが必要 */
  @Test
  fun loginFlow_step3_navigatesToMainScreenOnSuccess() {

    every { mockAmberSignerClient.checkAmberInstalled() } returns true
    every { mockAmberSignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.saveUser(any()) } returns Unit

    every { mockAmberSignerClient.handleAmberResponse(Activity.RESULT_OK, any()) } returns
        Result.success(
            io.github.omochice.pinosu.data.amber.AmberResponse(
                pubkey = testUser.pubkey, packageName = "com.greenart7c3.nostrsigner"))

    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    // Note: 実際のIntent結果処理はMainActivityのamberLauncherで行われるため、
    // ここではViewModelの状態変更を直接検証するのではなく、
    // UIの遷移結果を検証する

    // このテストではUI遷移の確認にとどめる
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()
  }

  @Test
  fun amberNotInstalledFlow_step1_displaysErrorDialog() {

    every { mockAmberSignerClient.checkAmberInstalled() } returns false

    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    composeTestRule
        .onNodeWithText("Amberアプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertIsDisplayed()

    composeTestRule.onNodeWithText("インストール").assertIsDisplayed()

    composeTestRule.onNodeWithText("閉じる").assertIsDisplayed()
  }

  @Test
  fun amberNotInstalledFlow_step2_dismissDialog() {

    every { mockAmberSignerClient.checkAmberInstalled() } returns false

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

    val maskedPubkey = "1234abcd...5678efgh" // 最初8文字...最後8文字のマスキング形式
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
