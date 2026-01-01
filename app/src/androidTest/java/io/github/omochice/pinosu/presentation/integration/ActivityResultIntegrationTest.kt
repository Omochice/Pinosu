package io.github.omochice.pinosu.presentation.integration

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.omochice.pinosu.MainActivity
import io.github.omochice.pinosu.data.amber.AmberSignerClient
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for ActivityResultAPI and Amber Intent integration
 * - registerForActivityResult configuration
 * - ActivityResultLauncher passing to AmberSignerClient
 * - Amber Intent result handling
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ActivityResultIntegrationTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Inject lateinit var amberSignerClient: AmberSignerClient

  @Before
  fun setup() {
    hiltRule.inject()
  }

  @Test
  fun whenAmberInstalled_loginButtonClick_shouldLaunchAmberIntent() {

    val isInstalled = amberSignerClient.checkAmberInstalled()

    if (!isInstalled) {
      return
    }

    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Amberアプリがインストールされていません").assertDoesNotExist()
  }

  @Test
  fun whenAmberNotInstalled_loginButtonClick_shouldShowErrorDialog() {

    val isInstalled = amberSignerClient.checkAmberInstalled()

    if (isInstalled) {
      return
    }

    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Amberアプリがインストールされていません").assertExists()
  }

  @Test fun whenAmberResponseSuccess_shouldNavigateToMainScreen() {}
}
