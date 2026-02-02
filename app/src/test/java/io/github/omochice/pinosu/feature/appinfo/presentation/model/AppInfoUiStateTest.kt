package io.github.omochice.pinosu.feature.appinfo.presentation.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [AppInfoUiState.versionDisplayText] formatting */
class AppInfoUiStateTest {

  @Test
  fun `versionDisplayText includes commit hash in parentheses`() {
    val uiState = AppInfoUiState(versionName = "0.3.0", commitHash = "abc1234")

    assertEquals("0.3.0 (abc1234)", uiState.versionDisplayText)
  }

  @Test
  fun `versionDisplayText omits hash when empty`() {
    val uiState = AppInfoUiState(versionName = "0.3.0", commitHash = "")

    assertEquals("0.3.0", uiState.versionDisplayText)
  }

  @Test
  fun `versionDisplayText omits hash when unknown`() {
    val uiState = AppInfoUiState(versionName = "0.3.0", commitHash = "unknown")

    assertEquals("0.3.0", uiState.versionDisplayText)
  }
}
