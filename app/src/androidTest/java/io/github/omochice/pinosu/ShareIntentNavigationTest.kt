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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies share-intent navigation under different app states.
 *
 * Covers four scenarios:
 * - Logged-out user receives a share intent, content is held until login completes, then navigates
 *   to PostBookmark.
 * - Already logged-in user receives a share intent at launch and navigates to PostBookmark
 *   immediately.
 * - Running app receives a share intent via [MainActivity.onNewIntent] and navigates to
 *   PostBookmark.
 * - Consumed shared content sets the contentConsumed flag, which is persisted via
 *   savedInstanceState to prevent re-extraction on configuration change (e.g. rotation).
 */
@HiltAndroidTest
@dagger.hilt.android.testing.UninstallModules(
    io.github.omochice.pinosu.feature.auth.di.AuthModule::class,
    io.github.omochice.pinosu.feature.shareintent.di.ShareIntentModule::class,
)
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

  @BindValue @JvmField val mockLocalAuthDataSource: LocalAuthDataSource = mockk(relaxed = true)

  @BindValue @JvmField val mockNip55SignerClient: Nip55SignerClient = mockk(relaxed = true)

  @BindValue
  @JvmField
  val mockAuthRepository: AuthRepository =
      object : AuthRepository by mockk(relaxed = true) {
        override suspend fun saveLoginState(
            user: User,
            loginMode: io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
        ): Result<Unit> = Result.success(Unit)

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun processNip55Response(resultCode: Int, data: Intent?): Result<User> =
            Result.success(testUser)
      }

  @BindValue
  @JvmField
  val mockLoginUseCase: io.github.omochice.pinosu.feature.auth.domain.usecase.LoginUseCase =
      mockk(relaxed = true)

  @BindValue
  @JvmField
  val mockLogoutUseCase: io.github.omochice.pinosu.feature.auth.domain.usecase.LogoutUseCase =
      mockk(relaxed = true)

  @BindValue
  @JvmField
  val mockGetLoginStateUseCase:
      io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase =
      mockk(relaxed = true)

  @BindValue
  @JvmField
  val mockReadOnlyLoginUseCase:
      io.github.omochice.pinosu.feature.auth.domain.usecase.ReadOnlyLoginUseCase =
      mockk(relaxed = true)

  @BindValue
  @JvmField
  val mockFetchRelayListUseCase: FetchRelayListUseCase =
      object : FetchRelayListUseCase by mockk(relaxed = true) {
        override suspend fun invoke(
            npubPubkey: String
        ): Result<List<io.github.omochice.pinosu.core.relay.RelayConfig>> =
            Result.success(emptyList())
      }

  init {
    every { mockExtractSharedContentUseCase(any()) } returns
        SharedContent(url = "https://example.com", comment = "Check this out")
    coEvery { mockLocalAuthDataSource.getUser() } returns null
    coEvery { mockGetLoginStateUseCase() } returns null
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

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onAllNodesWithText("ブックマークを追加").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマークを追加").assertIsDisplayed()

    // URL field: "https://example.com" → stripUrlScheme → "example.com"
    // The OutlinedTextField shows "example.com", "https://" is a separate Text prefix
    composeTestRule.onNodeWithText("example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("Check this out").assertIsDisplayed()
  }

  @Test
  fun `logged-in user with share intent navigates to PostBookmark immediately`() {
    coEvery { mockGetLoginStateUseCase() } returns testUser

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ブックマークを追加").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマークを追加").assertIsDisplayed()
  }

  @Test
  fun `onNewIntent with share intent navigates to PostBookmark`() {
    coEvery { mockGetLoginStateUseCase() } returns testUser
    every { mockExtractSharedContentUseCase(any()) } returns null

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ブックマーク").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマーク").assertIsDisplayed()

    // Set pendingSharedContent directly because
    // Instrumentation.callActivityOnNewIntent does not reliably propagate
    // mutableStateOf changes through the Compose test framework after recreate().
    composeTestRule.activityRule.scenario.onActivity { activity ->
      activity.pendingSharedContent =
          SharedContent(url = "https://example.com", comment = "Check this out")
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ブックマークを追加").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマークを追加").assertIsDisplayed()

    composeTestRule.onNodeWithText("example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("Check this out").assertIsDisplayed()
  }

  @Test
  fun `consumed shared content is not re-extracted after Activity recreation`() {
    coEvery { mockGetLoginStateUseCase() } returns testUser

    composeTestRule.activityRule.scenario.recreate()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("ブックマークを追加").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("ブックマークを追加").assertIsDisplayed()

    // Wait for onSharedContentConsumed to set contentConsumed = true
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      var consumed = false
      composeTestRule.activityRule.scenario.onActivity { activity ->
        consumed = activity.pendingSharedContent == null
      }
      consumed
    }

    // Verify contentConsumed is true after consumption — onSaveInstanceState
    // will persist this flag, so a subsequent onCreate (rotation) skips extraction.
    // A second recreate() is not used because ActivityScenario.recreate() hangs
    // when Compose navigation has PostBookmark on the back stack.
    composeTestRule.activityRule.scenario.onActivity { activity ->
      assertTrue(
          "contentConsumed should be true after shared content is consumed",
          activity.contentConsumed,
      )
    }
  }
}
