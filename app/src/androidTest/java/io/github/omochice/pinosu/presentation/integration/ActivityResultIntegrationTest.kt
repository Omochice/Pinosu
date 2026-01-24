package io.github.omochice.pinosu.presentation.integration

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.omochice.pinosu.MainActivity
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for ActivityResultAPI and NIP-55 signer Intent integration
 * - registerForActivityResult configuration
 * - ActivityResultLauncher passing to Nip55SignerClient
 * - NIP-55 signer Intent result handling
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ActivityResultIntegrationTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Inject lateinit var nip55SignerClient: Nip55SignerClient

  @Before
  fun setup() {
    hiltRule.inject()
  }

  @Test
  fun `when Nip55Signer installed login button click should launch Nip55Signer intent`() {

    val isInstalled = nip55SignerClient.checkNip55SignerInstalled()

    if (!isInstalled) {
      return
    }

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("NIP-55対応アプリが必要です").assertDoesNotExist()
  }

  @Test
  fun `when Nip55Signer not installed login button click should show error dialog`() {

    val isInstalled = nip55SignerClient.checkNip55SignerInstalled()

    if (isInstalled) {
      return
    }

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("NIP-55対応アプリが必要です").assertExists()
  }

  @org.junit.Ignore("TODO: Implement navigation verification after NIP-55 response")
  @Test
  fun `when Nip55Signer response success should navigate to main screen`() {}
}
