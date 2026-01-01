package io.github.omochice.pinosu.presentation.ui

import io.github.omochice.pinosu.presentation.viewmodel.LoginUiState
import org.junit.Rule
import org.junit.test
import roidx.compose.ui.test.assertIsDisplayed
import roidx.compose.ui.test.assertIsNotDisplayed
import roidx.compose.ui.test.junit4.createComposeRule
import roidx.compose.ui.test.onNodeWithText
import roidx.compose.ui.test.performClick


 @get:Rule val composetestRule = createComposeRule()

// ========== Login buttondisplayof ==========
 @test
 fun loginScreen_displaysLoginButton() {
// Given: Initial stateofLoginUiState val initialState = LoginUiState()

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(uiState = initialState, onLoginButtonClick = {}, onDismissDialog = {})
 }

// Then: "Login with Amber"is displayed composetestRule.onNodeWithText("Login with Amber").assertIsDisplayed()
 }

 @test
 fun loginScreen_loginButtonClickTriggersCallback() {
// Given: Initial stateofLoginUiState val initialState = LoginUiState()
 var clicked = false

// When: LoginScreendisplaybutton composetestRule.setContent {
 LoginScreen(
 uiState = initialState, onLoginButtonClick = { clicked = true }, onDismissDialog = {})
 }
 composetestRule.onNodeWithText("Login with Amber").performClick()

// Then: assert(clicked) { "Login button click should trigger callback" }
 }

// ========== display loading indicatorof ==========
 @test
 fun loginScreen_displaysLoadingIndicatorWhenLoading() {
// Given: isLoading = true ofLoginUiState val loadingState = LoginUiState(isLoading = true)

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(uiState = loadingState, onLoginButtonClick = {}, onDismissDialog = {})
 }

// Then: Loading indicatoris displayed composetestRule.onNodeWithText("Loading...").assertIsDisplayed()
 }

 @test
 fun loginScreen_hidesLoadingIndicatorWhenNotLoading() {
// Given: isLoading = false ofLoginUiState val notLoadingState = LoginUiState(isLoading = false)

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(uiState = notLoadingState, onLoginButtonClick = {}, onDismissDialog = {})
 }

// Then: Loading indicatordisplay composetestRule.onNodeWithText("Loading...").assertIsNotDisplayed()
 }

// ========== invalidoftest ==========
 @test
 fun loginScreen_disablesLoginButtonWhenLoading() {
// Given: isLoading = true ofLoginUiState val loadingState = LoginUiState(isLoading = true)
 var clickCount = 0

// When: LoginScreendisplaybutton composetestRule.setContent {
 LoginScreen(
 uiState = loadingState, onLoginButtonClick = { clickCount++ }, onDismissDialog = {})
 }
 composetestRule.onNodeWithText("Login with Amber").performClick()

// Then: invalidedingfor assert(clickCount == 0) { "Login button should be disabled when loading" }
 }

 @test
 fun loginScreen_displaysAmberInstallDialogWhenRequested() {
// Given: showAmberInstallDialog = true ofLoginUiState val dialogState = LoginUiState(showAmberInstallDialog = true)

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(uiState = dialogState, onLoginButtonClick = {}, onDismissDialog = {})
 }

// Then: AmberInstalldialogis displayed composetestRule.onNodeWithText("Amber app required").assertIsDisplayed()
 composetestRule.onNodeWithText("This app requires Amber app. Please install it.").assertIsDisplayed()
 }

 @test
 fun loginScreen_displaysGenericErrorDialogWhenErrorMessageExists() {
// Given: errorMessage setLoginUiState val errorState = LoginUiState(errorMessage = "testerrormessage")

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
 }

// Then: Error dialogis displayed composetestRule.onNodeWithText("error").assertIsDisplayed()
 composetestRule.onNodeWithText("testerrormessage").assertIsDisplayed()
 }

 @test
 fun loginScreen_dismissDialogCallsCallback() {
// Given: showAmberInstallDialog = true ofLoginUiState val dialogState = LoginUiState(showAmberInstallDialog = true)
 var dismissCalled = false

// When: LoginScreendisplaydialogClose composetestRule.setContent {
 LoginScreen(
 uiState = dialogState,
 onLoginButtonClick = {},
 onDismissDialog = { dismissCalled = true })
 }
 composetestRule.onNodeWithText("Close").performClick()

// Then: dismiss assert(dismissCalled) { "Dismiss dialog should trigger callback" }
 }

 @test
 fun loginScreen_installButtonOpensPlayStore() {
// Given: showAmberInstallDialog = true ofLoginUiState val dialogState = LoginUiState(showAmberInstallDialog = true)
 var installCalled = false

// When: LoginScreendisplayInstallbutton composetestRule.setContent {
 LoginScreen(
 uiState = dialogState,
 onLoginButtonClick = {},
 onDismissDialog = {},
 onInstallAmber = { installCalled = true })
 }
 composetestRule.onNodeWithText("Install").performClick()

// Then: install assert(installCalled) { "Install button should trigger callback" }
 }

 @test
 fun loginScreen_retryButtonCallsCallback() {
// Given: errormessageofLoginUiState val errorState = LoginUiState(errorMessage = "Login processing timed out.")
 var retryCalled = false

// When: LoginScreendisplaybutton composetestRule.setContent {
 LoginScreen(
 uiState = errorState,
 onLoginButtonClick = {},
 onDismissDialog = {},
 onRetry = { retryCalled = true })
 }
 composetestRule.onNodeWithText("Retry").performClick()

// Then: retry assert(retryCalled) { "Retry button should trigger callback" }
 }

 @test
 fun loginScreen_displaysSuccessMessageWhenLoginSucceeds() {
// Given: loginSuccess = true ofLoginUiState val successState = LoginUiState(loginSuccess = true)

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(
 uiState = successState,
 onLoginButtonClick = {},
 onDismissDialog = {},
 onNavigateToMain = {})
 }

// Then: login succeedsmessageis displayed composetestRule.onNodeWithText("Login succeeded").assertIsDisplayed()
 }

 @test
 fun loginScreen_triggersNavigationWhenLoginSucceeds() {
// Given: loginSuccess = true ofLoginUiState val successState = LoginUiState(loginSuccess = true)
 var navigationTriggered = false

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(
 uiState = successState,
 onLoginButtonClick = {},
 onDismissDialog = {},
 onNavigateToMain = { navigationTriggered = true })
 }

// Then: (LaunchedEffectbytransition) composetestRule.waitUntil(timeoutMillis = 1000) { navigationTriggered }
 assert(navigationTriggered) { "Navigation should be triggered when login succeeds" }
 }

 @test
 fun loginScreen_doesNotTriggerNavigationWhenLoginNotSuccessful() {
// Given: loginSuccess = false ofLoginUiState val notSuccessState = LoginUiState(loginSuccess = false)
 var navigationTriggered = false

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(
 uiState = notSuccessState,
 onLoginButtonClick = {},
 onDismissDialog = {},
 onNavigateToMain = { navigationTriggered = true })
 }

// Then: composetestRule.waitForIdle()
 assert(!navigationTriggered) {
 "Navigation should not be triggered when login is not successful"
 }
 }

 @test
 fun loginScreen_displaysUserRejectionErrorMessage() {
// Given: UsererrormessageofLoginUiState val errorState = LoginUiState(errorMessage = "Login cancelled. Please try again.")

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
 }

// Then: Usererrormessageis displayed composetestRule.onNodeWithText("Login cancelled. Please try again.").assertIsDisplayed()

// Then: OKis displayed (forbuttonis displayed) composetestRule.onNodeWithText("OK").assertIsDisplayed()
 }

 @test
 fun loginScreen_displaysTimeoutErrorWithRetryOption() {
// Given: errormessageofLoginUiState val errorState =
 LoginUiState(errorMessage = "Login processing timed out. Please verify Amber app and retry.")

// When: LoginScreendisplay composetestRule.setContent {
 LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
 }

// Then: errormessageis displayed composetestRule
 .onNodeWithText("Login processing timed out. Please verify Amber app and retry.")
 .assertIsDisplayed()

// Then: buttonis displayed composetestRule.onNodeWithText("Retry").assertIsDisplayed()

// Then: buttonis displayed composetestRule.onNodeWithText("Cancel").assertIsDisplayed()
 }
}
