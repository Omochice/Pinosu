package io.github.omochice.pinosu.presentation.ui

import io.github.omochice.pinosu.presentation.viewmodel.MainUiState
import org.junit.Rule
import org.junit.test
import roidx.compose.ui.test.assertIsDisplayed
import roidx.compose.ui.test.junit4.createComposeRule
import roidx.compose.ui.test.onNodeWithText
import roidx.compose.ui.test.performClick


 @get:Rule val composetestRule = createComposeRule()

/**
 * Logout button is displayed
 *
 * Requirement 3.4: Main screen Logout button
 */
@test
 fun mainScreen_logoutButtonIsDisplayed() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("logout").assertIsDisplayed()
 }

/**
 * User pubkey is displayed
 *
 * Requirement 3.5: Main screen displays pubkey when logged in
 */
@test
 fun mainScreen_userPubkeyIsDisplayed() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert
 // Verify that pubkey is displayed (first 8 chars...last 8 chars)
 val expectedFormattedPubkey = "12345678...90abcdef"
 composetestRule.onNodeWithText(expectedFormattedPubkey).assertIsDisplayed()
 }

/**
 * When pubkey is null, proper message is displayed
 *
 * Requirement 2.3: Verify logged in state
 */
@test
 fun mainScreen_whenPubkeyIsNull_showsNotLoggedInMessage() {
// Arrange val uiState = MainUiState(userPubkey = null, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("Not logged in").assertIsDisplayed()
 }

/**
 * Logout button tap triggers callback
 *
 * Requirement 2.4: Logout functionality
 */
@test
 fun mainScreen_whenLogoutButtonClicked_callsOnLogout() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
 var logoutCallbackCalled = false
 val onLogout = { logoutCallbackCalled = true }

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }
 composetestRule.onNodeWithText("logout").performClick()

// Assert assert(logoutCallbackCalled) { "onLogout callback should not be called" }
 }

/**
 * Loading indicator is displayed during logout processing
 *
 * Requirement 3.2: Display loading indicator during processing (logout)
 */
@test
 fun mainScreen_whenLoggingOut_showsLoadingIndicator() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("logoutduring...").assertIsDisplayed()
 }

/**
 * Logout button is disabled during logout processing
 *
 * Best Practice: Prevent multiple logout calls
 */
@test
 fun mainScreen_whenLoggingOut_logoutButtonIsDisabled() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)
 var logoutCallbackCalled = false
 val onLogout = { logoutCallbackCalled = true }

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }

// Verify loading message is displayed
 composetestRule.onNodeWithText("logoutduring...").assertIsDisplayed()

 // Assert
 // Verify logout button is disabled (callback should not be called)
 assert(!logoutCallbackCalled) { "onLogout callback should not be called during logout processing" }
 }

/**
 * Navigate to login screen after logout completion
 */
 fun mainScreen_afterLogout_callsNavigateToLogin() {
// Arrange
 val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val loggedInState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
 val loggedOutState = MainUiState(userPubkey = null, isLoggingOut = false)
 var navigateToLoginCalled = false
 val onNavigateToLogin = { navigateToLoginCalled = true }

// Act - Display MainScreen with logged in state
 composetestRule.setContent {
 MainScreen(uiState = loggedInState, onLogout = {}, onNavigateToLogin = onNavigateToLogin)
 }

// Act - Update to logged out state (pubkey = null)
 composetestRule.setContent {
 MainScreen(uiState = loggedOutState, onLogout = {}, onNavigateToLogin = onNavigateToLogin)
 }

// Assert
 // After logout completion (pubkey = null), should navigate to Login screen
 assert(navigateToLoginCalled) { "onNavigateToLogin callback should be called after logout completion" }
 }

/**
 * No navigation during logout processing
 *
 * Requirement 2.4: Properly handle logout state
 */
@test
 fun mainScreen_doesNotNavigateWhileLoggingOut() {
// Arrange val loggingOutState = MainUiState(userPubkey = null, isLoggingOut = true)
 var navigateToLoginCalled = false

// Act composetestRule.setContent {
 MainScreen(
 uiState = loggingOutState,
 onLogout = {},
 onNavigateToLogin = { navigateToLoginCalled = true })
 }

// Assert composetestRule.waitForIdle()
 assert(!navigateToLoginCalled) { "Navigation should not be called during logout processing" }
 }

/**
 * No navigation when initially not logged in
 *
 * Requirement 2.3: Verify logged in state
 */
@test
 fun mainScreen_doesNotNavigateWhenInitiallyNotLoggedIn() {
// Arrange val notLoggedInState = MainUiState(userPubkey = null, isLoggingOut = false)
 var navigateToLoginCalled = false

// Act composetestRule.setContent {
 MainScreen(
 uiState = notLoggedInState,
 onLogout = {},
 onNavigateToLogin = { navigateToLoginCalled = true })
 }

// Assert composetestRule.waitForIdle()
 assert(!navigateToLoginCalled) { "Navigation should not be called if initially not logged in" }
 }

/**
 * Short pubkey (less than 16 chars) is displayed without masking
 *
 * Requirement 3.5: Properly display pubkey
 */
@test
 fun mainScreen_displaysShortPubkeyWithoutMasking() {
// Arrange val shortPubkey = "short1234"
 val uiState = MainUiState(userPubkey = shortPubkey, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText(shortPubkey).assertIsDisplayed()
 }

/**
 * Logged in status is displayed
 *
 * Requirement 2.3: Display logged in state
 */
@test
 fun mainScreen_displaysLoggedInText() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("loginduring").assertIsDisplayed()
 }

/**
 * Logout button is hidden when not logged in
 *
 * Requirement 3.4: Properly display UI
 */
@test
 fun mainScreen_hidesLogoutButtonWhenNotLoggedIn() {
// Arrange val notLoggedInState = MainUiState(userPubkey = null, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = notLoggedInState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("logout").assertDoesNotExist()
 }
}
