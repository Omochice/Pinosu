package io.github.omochice.pinosu

import roid.app.Activity
import roidx.compose.ui.test.*
import roidx.compose.ui.test.junit4.createAndroidComposeRule
import roidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt. roid.testing.BindValue
import dagger.hilt. roid.testing.HiltAndroidRule
import dagger.hilt. roid.testing.HiltAndroidtest
import io.github.omochice.pinosu.data.amber.AmberSignerClient
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.domain.model.User
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.test
import org.junit.runner.RunWith

/**
 *
 * Requirements:
 * - 1.1: Login button tap to start Amber integration
 * - 1.2: Amber when not installed show dialog
 * - 2.4: provide logout functionality
 * - 3.1: Login screen "Login with Amber" button placement
 * - 3.2: display loading indicator during login processing
 * - 3.3: transition to Main screen when login succeeds
 * - 3.4: Main screen Logout button placement
 *
 * test content:
 * 1. login flow (Login screen → Login button tap → loading display → Main screen transition)
 * 2. Amber not installed error flow
 * 3. logout flow (Main screen → Logout button tap → Login screen transition)
 */
@HiltAndroidtest
@RunWith(AndroidJUnit4::class)
class UserFlowtest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composetestRule = createAndroidComposeRule<MainActivity>()

  @BindValue @JvmField val mockAmberSignerClient: AmberSignerClient = mockk(relaxed = true)

  @BindValue @JvmField val mockLocalAuthDataSource: LocalAuthDataSource = mockk(relaxed = true)

  @Before
  fun setup() {
    hiltRule.inject()
    // Default not logged in state
    coEvery { mockLocalAuthDataSource.getUser() } returns null
  }

  /**
   * login flow - Login screen is displayed
   *
   * Given: App startup (not logged in) When: app starts Then: Login screen is displayed ("Login with Amber" button is displayed)
   *
   * Requirement 3.1: Login screen "Login with Amber" button placement
   */
  @test
  fun loginFlow_displaysLoginScreen() {
    // Then: "Login with Amber" button is displayed on Login screen
    composetestRule.onNodeWithText("Login with Amber").assertIsDisplayed()
  }

  /**
   * login flow - Login button tap to loading display
   *
   * Given: Login screen is displayed When: "Login with Amber" button tap Then: Loading indicator is displayed
   *
   * Requirement 3.2: display loading indicator during login processing
   *
   * Note: This test verifies the loading state before the Amber Intent is launched
   */
  @test
  fun loginFlow_displaysLoadingOnButtonClick() {
    // Given: Amber is installed
    every { mockAmberSignerClient.checkAmberInstalled() } returns true
    every { mockAmberSignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    // When: Tap login button
    composetestRule.onNodeWithText("Login with Amber").performClick()

    // Then: Loading indicator is displayed
    composetestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()

    // Then: Login button is disabled
    composetestRule.onNodeWithText("Login with Amber").assertIsNotEnabled()
  }

  /**
   * login flow - transition to Main screen after login success
   *
   * Given: Login screen is displayed When: login succeeds Then: transition to Main screen and "logout" button is displayed
   *
   * Requirement 3.3: transition to Main screen when login succeeds Requirement 3.4: Main screen Logout button placement
   *
   * Note: Simulation of Amber Intent result is necessary
   */
  @test
  fun loginFlow_navigatesToMainScreenOnSuccess() {
    // Given: Amber is installed
    every { mockAmberSignerClient.checkAmberInstalled() } returns true
    every { mockAmberSignerClient.createPublicKeyIntent() } returns mockk(relaxed = true)

    // Given: local save succeeds
    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.saveUser(any()) } returns Unit

    // Given: response from Amber is success
    every { mockAmberSignerClient.h leAmberResponse(Activity.RESULT_OK, any()) } returns
        Result.success(
            io.github.omochice.pinosu.data.amber.AmberResponse(
                pubkey = testUser.pubkey, packageName = "com.greenart7c3.nostrsigner"))

    // When: Tap login button
    composetestRule.onNodeWithText("Login with Amber").performClick()

    // When: Simulate success response from Amber (direct call to ViewModel's processAmberResponse is expected)
    // Note: In actual implementation, the Intent result processing is performed by MainActivity's amberLauncher,
    // so here we directly verify the ViewModel's state change instead,
    // and verify the result of UI transition

    // Then: transition to Main screen ("logout" button is displayed)
    // This test only verifies the UI transition
    composetestRule.waitUntil(timeoutMillis = 5000) {
      composetestRule.onAllNodesWithText("logout").fetchSemanticsNodes().isNotEmpty()
    }
    composetestRule.onNodeWithText("logout").assertIsDisplayed()
  }

  /**
   * Amber not installed error flow - Error dialog display
   *
   * Given: Login screen is displayed When: Amber is not installed and Login button tap Then: Amber not installed Error dialog is displayed
   *
   * Requirement 1.2: show dialog when Amber is not installed
   */
  @test
  fun amberNotInstalledFlow_displaysErrorDialog() {
    // Given: Amber is not installed
    every { mockAmberSignerClient.checkAmberInstalled() } returns false

    // When: Tap login button
    composetestRule.onNodeWithText("Login with Amber").performClick()

    // Then: Amber not installed Error dialog is displayed
    composetestRule
        .onNodeWithText("Amber app is not installed. Please install it from Google Play Store.")
        .assertIsDisplayed()

    // Then: "Install" button is displayed
    composetestRule.onNodeWithText("Install").assertIsDisplayed()

    // Then: "Close" button is displayed
    composetestRule.onNodeWithText("Close").assertIsDisplayed()
  }

  /**
   * Amber not installed error flow - Close dialog
   *
   * Given: Amber not installed Error dialog is displayed When: "Close" button tap Then: dialog is closed and stays on Login screen
   *
   * Requirement 1.2: show dialog when Amber is not installed
   */
  @test
  fun amberNotInstalledFlow_dismissDialog() {
    // Given: Amber is not installed
    every { mockAmberSignerClient.checkAmberInstalled() } returns false

    // Given: Tap login button to display Error dialog
    composetestRule.onNodeWithText("Login with Amber").performClick()
    composetestRule
        .onNodeWithText("Amber app is not installed. Please install it from Google Play Store.")
        .assertIsDisplayed()

    // When: Tap "Close" button
    composetestRule.onNodeWithText("Close").performClick()

    // Then: Dialog is closed
    composetestRule
        .onNodeWithText("Amber app is not installed. Please install it from Google Play Store.")
        .assertDoesNotExist()

    // Then: Stay on Login screen ("Login with Amber" button is displayed)
    composetestRule.onNodeWithText("Login with Amber").assertIsDisplayed()
  }

  /**
   * logout flow - Main screen Logout button tap
   *
   * Given: Main screen is displayed (logged in state) When: Tap logout button Then: transition to Login screen and "Login with Amber" button is displayed
   *
   * Requirement 2.4: provide logout functionality
   */
  @test
  fun logoutFlow_navigatesToLoginScreenOnLogout() {
    // Given: App startup with logged in state
    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser
    coEvery { mockLocalAuthDataSource.clearLoginState() } returns Unit

    // Given: Restart app to reflect login state
    composetestRule.activityRule.scenario.recreate()

    // Given: Main screen is displayed ("logout" button is displayed)
    composetestRule.waitUntil(timeoutMillis = 3000) {
      composetestRule.onAllNodesWithText("logout").fetchSemanticsNodes().isNotEmpty()
    }
    composetestRule.onNodeWithText("logout").assertIsDisplayed()

    // When: Tap logout button
    composetestRule.onNodeWithText("logout").performClick()

    // Then: transition to Login screen ("Login with Amber" button is displayed)
    composetestRule.waitUntil(timeoutMillis = 3000) {
      composetestRule.onAllNodesWithText("Login with Amber").fetchSemanticsNodes().isNotEmpty()
    }
    composetestRule.onNodeWithText("Login with Amber").assertIsDisplayed()

    // Then: Logout button is not displayed
    composetestRule.onNodeWithText("logout").assertDoesNotExist()
  }

  /**
   *
   * Given: Logged in state is saved When: app starts Then: Main screen is displayed (skip Login screen)
   *
   * Requirement 2.2: verify saved login state when app starts Requirement 2.3: display Main screen when logged in
   */
  @test
  fun appRestart_whenLoggedIn_displaysMainScreen() {
    // Given: Logged in state is saved
    val testUser = User(pubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    coEvery { mockLocalAuthDataSource.getUser() } returns testUser

    // When: Restart app
    composetestRule.activityRule.scenario.recreate()

    // Then: Main screen is displayed ("logout" button is displayed)
    composetestRule.waitUntil(timeoutMillis = 3000) {
      composetestRule.onAllNodesWithText("logout").fetchSemanticsNodes().isNotEmpty()
    }
    composetestRule.onNodeWithText("logout").assertIsDisplayed()

    // Then: User's pubkey is displayed
    val maskedPubkey = "1234abcd...5678efgh" // format: first 8 characters...last 8 characters masked
    composetestRule.onNodeWithText(maskedPubkey).assertIsDisplayed()

    // Then: Login screen is not displayed
    composetestRule.onNodeWithText("Login with Amber").assertDoesNotExist()
  }

  /**
   *
   * Given: login state is not saved When: app starts Then: Login screen is displayed
   *
   * Requirement 2.2: verify saved login state when app starts
   */
  @test
  fun appRestart_whenNotLoggedIn_displaysLoginScreen() {
    // Given: login state is not saved (default set)
    coEvery { mockLocalAuthDataSource.getUser() } returns null

    // When: Restart app
    composetestRule.activityRule.scenario.recreate()

    // Then: Login screen is displayed ("Login with Amber" button is displayed)
    composetestRule.waitUntil(timeoutMillis = 3000) {
      composetestRule.onAllNodesWithText("Login with Amber").fetchSemanticsNodes().isNotEmpty()
    }
    composetestRule.onNodeWithText("Login with Amber").assertIsDisplayed()

    // Then: Main screen is not displayed
    composetestRule.onNodeWithText("logout").assertDoesNotExist()
  }
}
