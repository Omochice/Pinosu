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

  /**
   * Test 1: ログインフロー - ログイン画面が表示されること
   *
   * Given: アプリを起動（未ログイン状態） When: アプリが起動する Then: ログイン画面が表示される（「Amberでログイン」ボタンが表示される）
   */
  @Test
  fun loginFlow_step1_displaysLoginScreen() {
    // Then: ログイン画面の「Amberでログイン」ボタンが表示されている
    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()
  }

  /**
   * Test 2: ログインフロー - ログインボタンタップでローディング表示
   *
   * Given: ログイン画面が表示されている When: 「Amberでログイン」ボタンをタップ Then: ローディングインジケーターが表示される
   *
   * Note: このテストはAmber Intentが起動される前のローディング状態を確認する
   */
  @Test
  fun loginFlow_step2_displaysLoadingOnButtonClick() {
    // Given: Amberがインストールされている
    every { mockAmberSignerClient.checkAmberInstalled() } returns true
    every { mockAmberSignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    // When: ログインボタンをタップ
    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    // Then: ローディングインジケーターが表示される
    composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()

    // Then: ログインボタンが無効化される
    composeTestRule.onNodeWithText("Amberでログイン").assertIsNotEnabled()
  }

  /**
   * Test 3: ログインフロー - ログイン成功後にメイン画面に遷移
   *
   * Given: ログイン画面が表示されている When: ログインに成功する Then: メイン画面に遷移し、「ログアウト」ボタンが表示される
   *
   * Note: Amber Intent結果のシミュレーションが必要
   */
  @Test
  fun loginFlow_step3_navigatesToMainScreenOnSuccess() {
    // Given: Amberがインストールされている
    every { mockAmberSignerClient.checkAmberInstalled() } returns true
    every { mockAmberSignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    // Given: ローカル保存が成功する
    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.saveUser(any()) } returns Unit

    // Given: Amberからの応答が成功
    every { mockAmberSignerClient.handleAmberResponse(Activity.RESULT_OK, any()) } returns
        Result.success(
            io.github.omochice.pinosu.data.amber.AmberResponse(
                pubkey = testUser.pubkey, packageName = "com.greenart7c3.nostrsigner"))

    // When: ログインボタンをタップ
    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    // When: Amberからの成功レスポンスをシミュレート（ViewModelのprocessAmberResponseを直接呼び出す想定）
    // Note: 実際のIntent結果処理はMainActivityのamberLauncherで行われるため、
    // ここではViewModelの状態変更を直接検証するのではなく、
    // UIの遷移結果を検証する

    // Then: メイン画面に遷移している（「ログアウト」ボタンが表示される）

    // このテストではUI遷移の確認にとどめる
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()
  }

  /**
   * Test 4: Amber未インストールエラーフロー - エラーダイアログ表示
   *
   * Given: ログイン画面が表示されている When: Amberがインストールされていない状態でログインボタンをタップ Then: Amber未インストールエラーダイアログが表示される
   */
  @Test
  fun amberNotInstalledFlow_step1_displaysErrorDialog() {
    // Given: Amberがインストールされていない
    every { mockAmberSignerClient.checkAmberInstalled() } returns false

    // When: ログインボタンをタップ
    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    // Then: Amber未インストールエラーダイアログが表示される
    composeTestRule
        .onNodeWithText("Amberアプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertIsDisplayed()

    // Then: 「インストール」ボタンが表示される
    composeTestRule.onNodeWithText("インストール").assertIsDisplayed()

    // Then: 「閉じる」ボタンが表示される
    composeTestRule.onNodeWithText("閉じる").assertIsDisplayed()
  }

  /**
   * Test 5: Amber未インストールエラーフロー - ダイアログを閉じる
   *
   * Given: Amber未インストールエラーダイアログが表示されている When: 「閉じる」ボタンをタップ Then: ダイアログが閉じられ、ログイン画面に留まる
   */
  @Test
  fun amberNotInstalledFlow_step2_dismissDialog() {
    // Given: Amberがインストールされていない
    every { mockAmberSignerClient.checkAmberInstalled() } returns false

    // Given: ログインボタンをタップしてエラーダイアログを表示
    composeTestRule.onNodeWithText("Amberでログイン").performClick()
    composeTestRule
        .onNodeWithText("Amberアプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertIsDisplayed()

    // When: 「閉じる」ボタンをタップ
    composeTestRule.onNodeWithText("閉じる").performClick()

    // Then: ダイアログが閉じられる
    composeTestRule
        .onNodeWithText("Amberアプリがインストールされていません。Google Play Storeからインストールしてください。")
        .assertDoesNotExist()

    // Then: ログイン画面に留まる（「Amberでログイン」ボタンが表示されている）
    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()
  }

  /**
   * Test 6: ログアウトフロー - メイン画面でログアウトボタンをタップ
   *
   * Given: メイン画面が表示されている（ログイン済み状態） When: ログアウトボタンをタップ Then: ログイン画面に遷移し、「Amberでログイン」ボタンが表示される
   */
  @Test
  fun logoutFlow_step1_navigatesToLoginScreenOnLogout() {
    // Given: ログイン済み状態でアプリを起動
    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser
    coEvery { mockLocalAuthDataSource.clearLoginState() } returns Unit

    // Given: アプリを再起動してログイン状態を反映
    composeTestRule.activityRule.scenario.recreate()

    // Given: メイン画面が表示されている（「ログアウト」ボタンが表示される）
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()

    // When: ログアウトボタンをタップ
    composeTestRule.onNodeWithText("ログアウト").performClick()

    // Then: ログイン画面に遷移している（「Amberでログイン」ボタンが表示される）
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Amberでログイン").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()

    // Then: ログアウトボタンは表示されていない
    composeTestRule.onNodeWithText("ログアウト").assertDoesNotExist()
  }

  /** Given: ログイン済み状態が保存されている When: アプリを起動する Then: メイン画面が表示される（ログイン画面をスキップ） */
  @Test
  fun appRestart_whenLoggedIn_displaysMainScreen() {
    // Given: ログイン済み状態が保存されている
    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser

    // When: アプリを再起動
    composeTestRule.activityRule.scenario.recreate()

    // Then: メイン画面が表示される（「ログアウト」ボタンが表示される）
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("ログアウト").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()

    // Then: ユーザーのpubkeyが表示される
    val maskedPubkey = "1234abcd...5678efgh" // 最初8文字...最後8文字のマスキング形式
    composeTestRule.onNodeWithText(maskedPubkey).assertIsDisplayed()

    // Then: ログイン画面は表示されていない
    composeTestRule.onNodeWithText("Amberでログイン").assertDoesNotExist()
  }

  /** Given: ログイン状態が保存されていない When: アプリを起動する Then: ログイン画面が表示される */
  @Test
  fun appRestart_whenNotLoggedIn_displaysLoginScreen() {
    // Given: ログイン状態が保存されていない（デフォルト設定）
    coEvery { mockLocalAuthDataSource.getUser() } returns null

    // When: アプリを再起動
    composeTestRule.activityRule.scenario.recreate()

    // Then: ログイン画面が表示される（「Amberでログイン」ボタンが表示される）
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Amberでログイン").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()

    // Then: メイン画面は表示されていない
    composeTestRule.onNodeWithText("ログアウト").assertDoesNotExist()
  }
}
