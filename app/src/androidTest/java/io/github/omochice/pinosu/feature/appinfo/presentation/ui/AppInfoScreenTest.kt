package io.github.omochice.pinosu.feature.appinfo.presentation.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.feature.appinfo.presentation.model.AppInfoUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for [AppInfoScreen] */
class AppInfoScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displaysFormattedVersionWithCommitHash() {
    val uiState = AppInfoUiState(versionName = "1.0.0", commitHash = "def5678")

    composeTestRule.setContent { AppInfoScreen(uiState = uiState, onNavigateUp = {}) }

    composeTestRule.onNodeWithText("1.0.0 (def5678)").assertIsDisplayed()
  }

  @Test
  fun copyButtonIsDisplayed() {
    val uiState = AppInfoUiState(versionName = "1.0.0", commitHash = "def5678")

    composeTestRule.setContent { AppInfoScreen(uiState = uiState, onNavigateUp = {}) }

    composeTestRule.onNodeWithContentDescription("バージョン情報をコピー").assertIsDisplayed()
  }

  @Test
  fun displaysRepositoryUrl() {
    val uiState = AppInfoUiState(versionName = "1.0.0", commitHash = "def5678")

    composeTestRule.setContent { AppInfoScreen(uiState = uiState, onNavigateUp = {}) }

    composeTestRule.onNodeWithText("https://github.com/Omochice/Pinosu").assertIsDisplayed()
  }

  @Test
  fun tappingRepositoryUrlOpensUriHandler() {
    val openedUris = mutableListOf<String>()
    val fakeUriHandler =
        object : UriHandler {
          override fun openUri(uri: String) {
            openedUris.add(uri)
          }
        }
    val uiState = AppInfoUiState(versionName = "1.0.0", commitHash = "def5678")

    composeTestRule.setContent {
      CompositionLocalProvider(LocalUriHandler provides fakeUriHandler) {
        AppInfoScreen(uiState = uiState, onNavigateUp = {})
      }
    }

    composeTestRule.onNodeWithText("https://github.com/Omochice/Pinosu").performClick()

    assertEquals(listOf("https://github.com/Omochice/Pinosu"), openedUris)
  }
}
