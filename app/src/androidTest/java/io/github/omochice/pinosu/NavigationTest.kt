package io.github.omochice.pinosu

import dagger.hilt.roid.testing.HiltAndroidRule
import dagger.hilt.roid.testing.HiltAndroidtest
import io.github.omochice.pinosu.presentation.viewmodel.LoginViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.test
import roidx.compose.ui.test.assertIsDisplayed
import roidx.compose.ui.test.junit4.createAndroidComposeRule
import roidx.compose.ui.test.onNodeWithText
import roidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class Navigationtest {

 @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

 @get:Rule(order = 1) val composetestRule = createAndroidComposeRule<MainActivity>()

 private lateinit var viewModel: LoginViewModel

 @Before
 fun setup() {
 hiltRule.inject()
 }

/**
 * not logged in state app starts Login screen is displayed
 *
 * Given: Not logged in state When: app starts Then: Login screen is displayed ("Login with Amber" button is displayed)
 *
 * Requirement 2.2: app starts when saved login state verify
 */
@test
 fun navigation_whenNotLoggedIn_displaysLoginScreen() {
// Then: Login screenof"Login with Amber"is displayeding composetestRule.onNodeWithText("Login with Amber").assertIsDisplayed()
 }

 fun navigation_whenLoginSuccess_navigatesToMainScreen() {

/**
 * Main screen Logout button tap Login screen transition
 *
 * Given: Main screen is displayeding (logged in state) When: Tap logout button Then: Login screen transition "Login with Amber" button is displayed
 *
 * Requirement 2.4: logout functionality
 *
 * Note: login state before setup
 */
@test
 fun navigation_whenLogout_navigatesToLoginScreen() {
// Note: login stateof// tests, after logoutofnavigationverification// at this pointskip (implementationaftervalid)// TODO: login stateofimplementationaftervalid }

/**
 * Main screen Back button Login screen
 *
 * Given: Main screen is displayeding (logged in state) When: Back button Then: app completion (Login screen)
 *
 * Requirement: (logout Login screen)
 *
 * Note: Back Press processing Implementation after test
 */
@test
 fun navigation_onBackPressFromMainScreen_exitsApp() {
// Note: Back PressprocessingImplementation of// tests, Main screenofBack Pressverification// at this pointskip (implementationaftervalid)// TODO: Back Pressprocessingaftervalid }

/**
 * Login screen Back button app completion
 *
 * Given: Login screen is displayeding When: Back button Then: app completion
 *
 * Requirement: (Login screen)
 */
@test
 fun navigation_onBackPressFromLoginScreen_exitsApp() {
// Note: Back PressprocessingImplementation of// tests, Login screenofBack Pressverification// at this pointskip (implementationaftervalid)// TODO: Back Pressprocessingaftervalid }
}
