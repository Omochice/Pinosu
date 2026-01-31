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
import io.github.omochice.pinosu.core.relay.RelayConfig
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
 * Verifies that pending shared content is consumed after login completes.
 *
 * When the app receives ACTION_SEND while logged out, the shared content must be held until login
 * completes and then navigate to PostBookmark.
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
  val mockExtractSharedContentUseCase: ExtractSharedContentUseCase = mockk {
    every { invoke(any<Intent>()) } returns SharedContent(url = "https://example.com")
  }

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

  @BindValue
  @JvmField
  val mockFetchRelayListUseCase: FetchRelayListUseCase = mockk {
    coEvery { invoke(any<String>()) } returns Result.success(emptyList<RelayConfig>())
  }

  @Before
  fun setup() {
    hiltRule.inject()
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
}
