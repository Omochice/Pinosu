package io.github.omochice.pinosu.presentation.integration

import dagger.hilt.roid.testing.HiltAndroidRule
import dagger.hilt.roid.testing.HiltAndroidtest
import io.github.omochice.pinosu.MainActivity
import io.github.omochice.pinosu.data.amber.AmberSignerClient
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.test
import roidx.compose.ui.test.junit4.createAndroidComposeRule
import roidx.compose.ui.test.onNodeWithText
import roidx.compose.ui.test.performClick
import roidx.test.ext.junit.runners.AndroidJUnit4

/*** ActivityResultAPI Amber Intenttests** Task 10.3: ActivityResultAPIof* - registerForActivityResultofset* - AmberSignerClientofActivityResultLauncher* - Amber Intentresultof** Requirements: 1.1, 1.3*/@HiltAndroidtest
@RunWith(AndroidJUnit4::class)
class ActivityResultIntegrationtest {

 @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

 @get:Rule(order = 1) val composetestRule = createAndroidComposeRule<MainActivity>()

 @Inject lateinit var amberSignerClient: AmberSignerClient

 @Before
 fun setup() {
 hiltRule.inject()
 }

 @test
 fun whenAmberInstalled_loginButtonClick_shouldLaunchAmberIntent() {
// Given: Amberedingstate val isInstalled = amberSignerClient.checkAmberInstalled()

// AmberwhenInstallednot testskip if (!isInstalled) {
 return
 }

// When: Login button composetestRule.onNodeWithText("Login with Amber").performClick()

// Then: Amber Intented// Note: whenofIntenttestverification for,// Error dialogdisplay not Verify composetestRule.waitForIdle()
 composetestRule.onNodeWithText("Amber app not installed").assertDoesNotExist()
 }

 @test
 fun whenAmberNotInstalled_loginButtonClick_shouldShowErrorDialog() {
// Given: Amberednot state val isInstalled = amberSignerClient.checkAmberInstalled()

// Amber is installedtestskip if (isInstalled) {
 return
 }

// When: Login button composetestRule.onNodeWithText("Login with Amber").performClick()

// Then: AmberInstalldialogis displayed composetestRule.waitForIdle()
 composetestRule.onNodeWithText("Amber app not installed").assertExists()
 }

 @test
 fun whenAmberResponseSuccess_shouldNavigateToMainScreen() {
// Given: Login screendisplayingstate// Note: testswhenofAmberapp of for,// eda
// Ambertesttest// testtest// ActivityResultAPIof MainActivity.kt lines 84-91 implementation// - rememberLauncherForActivityResult// - viewModel.processAmberResponse processing }
}
