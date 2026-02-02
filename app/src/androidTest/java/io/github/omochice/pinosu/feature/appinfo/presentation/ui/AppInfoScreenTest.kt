package io.github.omochice.pinosu.feature.appinfo.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.omochice.pinosu.feature.appinfo.presentation.model.AppInfoUiState
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
}
