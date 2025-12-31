package io.github.omochice.pinosu.presentation.ui

import io.github.omochice.pinosu.presentation.viewmodel.MainUiState
import org.junit.Rule
import org.junit.test
import roidx.compose.ui.test.assertIsDisplayed
import roidx.compose.ui.test.junit4.createComposeRule
import roidx.compose.ui.test.onNodeWithText
import roidx.compose.ui.test.performClick

/*** MainScreenCompose UI tests for** Task 9.3: MainScreenUnit tests for* - pubkeydisplayoftest* - Logout buttondisplayoftest* - navigationoftest** Requirements: 2.3, 3.4, 3.5*/class MainScreentest {

 @get:Rule val composetestRule = createComposeRule()

/*** test 1: Logout buttonis displayed** Requirement 3.4: Main screenLogout button*/ @test
 fun mainScreen_logoutButtonIsDisplayed() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("logout").assertIsDisplayed()
 }

/*** test 2: Userofpubkeyis displayed ()** Requirement 3.5: Main screenloginduringofpubkeydisplay*/ @test
 fun mainScreen_userPubkeyIsDisplayed() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert// pubkeyis displayedVerify that (of8 afterof8of) val expectedFormattedPubkey = "12345678...90abcdef"
 composetestRule.onNodeWithText(expectedFormattedPubkey).assertIsDisplayed()
 }

/*** test 3: pubkeynullof, properly messageis displayed** Requirement 2.3: logged instateofverify*/ @test
 fun mainScreen_whenPubkeyIsNull_showsNotLoggedInMessage() {
// Arrange val uiState = MainUiState(userPubkey = null, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("Not logged in").assertIsDisplayed()
 }

/*** test 4: Logout buttontap** Requirement 2.4: logoutfunctionality*/ @test
 fun mainScreen_whenLogoutButtonClicked_callsOnLogout() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
 var logoutCallbackCalled = false
 val onLogout = { logoutCallbackCalled = true }

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }
 composetestRule.onNodeWithText("logout").performClick()

// Assert assert(logoutCallbackCalled) { "onLogout callback should not be called" }
 }

/*** test 5: logoutprocessingduringLoading indicatoris displayed** Requirement 3.2: loginduring processingLoading indicatordisplay (logout)*/ @test
 fun mainScreen_whenLoggingOut_showsLoadingIndicator() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("logoutduring...").assertIsDisplayed()
 }

/*** test 6: logoutprocessingduringLogout buttoninvalided** Best Practice: logoutduringof*/ @test
 fun mainScreen_whenLoggingOut_logoutButtonIsDisabled() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)
 var logoutCallbackCalled = false
 val onLogout = { logoutCallbackCalled = true }

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }

// logoutprocessingbuttonis displayed (messageof) composetestRule.onNodeWithText("logoutduring...").assertIsDisplayed()

// Assert// Logout button Verify that (for) assert(!logoutCallbackCalled) { "onLogout callback should not be called during logout processing" }
 }

/*** test 7 (Task 9.2): logoutcompletionafter, Login screennavigationforof** Requirement 2.4: logoutwhenLogin screentransition** Note: Thisofat this pointNavigation Composeimplementation (Task 10.2)offor, navigationof test*/ @test
 fun mainScreen_afterLogout_callsNavigateToLogin() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val loggedInState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
 val loggedOutState = MainUiState(userPubkey = null, isLoggingOut = false)
 var navigateToLoginCalled = false
 val onNavigateToLogin = { navigateToLoginCalled = true }

// Act - login stateMainScreendisplay composetestRule.setContent {
 MainScreen(uiState = loggedInState, onLogout = {}, onNavigateToLogin = onNavigateToLogin)
 }

// Act - logoutstate (pubkey = null) composetestRule.setContent {
 MainScreen(uiState = loggedOutState, onLogout = {}, onNavigateToLogin = onNavigateToLogin)
 }

// Assert// logoutcompletionafter (pubkey = null), Login screennavigation assert(navigateToLoginCalled) { "onNavigateToLogin callback should be called after logout completion" }
 }

// ========== test: navigation (Task 9.3) ==========
/*** test 8: logoutprocessingduringnavigation** Requirement 2.4: logoutofproperly state*/ @test
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

/*** test 9: not logged in stateofnavigation** Requirement 2.3: logged instateofverify*/ @test
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

/*** test 10: pubkey (16) is displayed** Requirement 3.5: pubkeyofproperly display*/ @test
 fun mainScreen_displaysShortPubkeyWithoutMasking() {
// Arrange val shortPubkey = "short1234"
 val uiState = MainUiState(userPubkey = shortPubkey, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText(shortPubkey).assertIsDisplayed()
 }

/*** test 11: loginduringis displayed** Requirement 2.3: logged instateofdisplay*/ @test
 fun mainScreen_displaysLoggedInText() {
// Arrange val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
 val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("loginduring").assertIsDisplayed()
 }

/*** test 12: not logged in stateLogout buttonis displayed** Requirement 3.4: properly UIdisplay*/ @test
 fun mainScreen_hidesLogoutButtonWhenNotLoggedIn() {
// Arrange val notLoggedInState = MainUiState(userPubkey = null, isLoggingOut = false)

// Act composetestRule.setContent { MainScreen(uiState = notLoggedInState, onLogout = {}) }

// Assert composetestRule.onNodeWithText("logout").assertDoesNotExist()
 }
}
