package io.github.omochice.pinosu

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.auth.data.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.usecase.FetchRelayListUseCase
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginViewModel
import io.github.omochice.pinosu.feature.shareintent.domain.model.SharedContent
import io.github.omochice.pinosu.feature.shareintent.domain.usecase.ExtractSharedContentUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies share-intent navigation under different app states.
 *
 * Covers three scenarios:
 * - Logged-out user receives a share intent, content is held until login completes, then navigates
 *   to PostBookmark.
 * - Already logged-in user receives a share intent at launch and navigates to PostBookmark
 *   immediately.
 * - Running app receives a share intent via [MainActivity.onNewIntent] and navigates to
 *   PostBookmark.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ShareIntentNavigationTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  private val testUser =
      User(
          pubkey =
              requireNotNull(Pubkey.parse("npub1" + "1234567890abcdef".repeat(3) + "1234567890a")))

  @BindValue
  @JvmField
  val mockExtractSharedContentUseCase: ExtractSharedContentUseCase = mockk(relaxed = true)

  @BindValue
  @JvmField
  val mockLocalAuthDataSource: LocalAuthDataSource =
      mockk(relaxed = true) { coEvery { getUser() } returns null }

  @BindValue @JvmField val mockNip55SignerClient: Nip55SignerClient = mockk(relaxed = true)

  @BindValue
  @JvmField
  val mockAuthRepository: AuthRepository =
      mockk(relaxed = true) {
        coEvery { getLoginState() } returns null
        coEvery { processNip55Response(any(), any()) } returns Result.success(testUser)
      }

  @BindValue @JvmField val mockFetchRelayListUseCase: FetchRelayListUseCase = mockk(relaxed = true)

  @Before
  fun setup() {
    hiltRule.inject()
    every { mockExtractSharedContentUseCase(any()) } returns
        SharedContent(url = "https://example.com")
    coEvery { mockFetchRelayListUseCase(any()) } returns Result.success(emptyList())
  }

  @Test
  fun `shared content navigates to PostBookmark after login`() {
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()

    val loginViewModel = ViewModelProvider(composeTestRule.activity)[LoginViewModel::class.java]
    loginViewModel.processNip55Response(Activity.RESULT_OK, Intent())

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ブックマークを追加").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマークを追加").assertIsDisplayed()
  }

  @Test
  fun `logged-in user with share intent navigates to PostBookmark immediately`() {
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser
    coEvery { mockAuthRepository.getLoginState() } returns testUser

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ブックマークを追加").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマークを追加").assertIsDisplayed()
  }

  @Test
  fun `onNewIntent with share intent navigates to PostBookmark`() {
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser
    coEvery { mockAuthRepository.getLoginState() } returns testUser
    every { mockExtractSharedContentUseCase(any<Intent>()) } returns null

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ブックマーク").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマーク").assertIsDisplayed()

    every { mockExtractSharedContentUseCase(any<Intent>()) } returns
        SharedContent(url = "https://example.com")

    composeTestRule.activityRule.scenario.onActivity { activity -> activity.onNewIntent(Intent()) }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ブックマークを追加").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマークを追加").assertIsDisplayed()
  }
}
