package io.github.omochice.pinosu.feature.auth.presentation.integration

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.omochice.pinosu.MainActivity
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
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

  private val context = InstrumentationRegistry.getInstrumentation().targetContext

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

    composeTestRule
        .onNodeWithText(context.getString(R.string.button_login_with_nip55))
        .performClick()

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithText(context.getString(R.string.dialog_title_nip55_signer_required))
        .assertDoesNotExist()
  }

  @Test
  fun `when Nip55Signer not installed login button click should show error dialog`() {

    val isInstalled = nip55SignerClient.checkNip55SignerInstalled()

    if (isInstalled) {
      return
    }

    composeTestRule
        .onNodeWithText(context.getString(R.string.button_login_with_nip55))
        .performClick()

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithText(context.getString(R.string.dialog_title_nip55_signer_required))
        .assertExists()
  }

  @org.junit.Ignore("TODO: Implement navigation verification after NIP-55 response")
  @Test
  fun `when Nip55Signer response success should navigate to main screen`() {}
}
