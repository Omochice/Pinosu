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

/*** test 1: not logged in stateapp starts Login screenis displayed** Given: Not logged in state When: app starts Then: Login screen is displayed ("Login with Amber"buttonis* displayed)** Requirement 2.2: app startswhensaveedlogin stateverify*/ @test
 fun navigation_whenNotLoggedIn_displaysLoginScreen() {
// Then: Login screenof"Login with Amber"is displayeding composetestRule.onNodeWithText("Login with Amber").assertIsDisplayed()
 }

 fun navigation_whenLoginSuccess_navigatesToMainScreen() {

/*** test 3: Main screenLogout buttontapLogin screentransitionthat** Given: Main screenis displayeding (logged instate) When: Tap logout button Then: Login* screentransition"Login with Amber"buttonis displayed** Requirement 2.4: logoutfunctionality** Note: login stateofbeforesetup*/ @test
 fun navigation_whenLogout_navigatesToLoginScreen() {
// Note: login stateof// tests, after logoutofnavigationverification// at this pointskip (implementationaftervalid)// TODO: login stateofimplementationaftervalid }

/*** test 4: Main screenBackbuttonLogin screen** Given: Main screenis displayeding (logged instate) When: ofBackbutton Then: appcompletion (Login* screen)** Requirement: (logoutLogin screen)** Note: Back PressprocessingImplementation ofaftertest*/ @test
 fun navigation_onBackPressFromMainScreen_exitsApp() {
// Note: Back PressprocessingImplementation of// tests, Main screenofBack Pressverification// at this pointskip (implementationaftervalid)// TODO: Back Pressprocessingaftervalid }

/*** test 5: Login screenBackbuttonappcompletionthat** Given: Login screenis displayeding When: ofBackbutton Then: appcompletion** Requirement: (Login screen)*/ @test
 fun navigation_onBackPressFromLoginScreen_exitsApp() {
// Note: Back PressprocessingImplementation of// tests, Login screenofBack Pressverification// at this pointskip (implementationaftervalid)// TODO: Back Pressprocessingaftervalid }
}
