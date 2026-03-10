package io.github.omochice.pinosu.feature.settings.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageModeTest {

  @Test
  fun `LanguageMode has exactly three values`() {
    assertEquals(3, LanguageMode.entries.size)
  }

  @Test
  fun `LanguageMode contains System value`() {
    assertEquals(LanguageMode.System, LanguageMode.valueOf("System"))
  }

  @Test
  fun `LanguageMode contains English value`() {
    assertEquals(LanguageMode.English, LanguageMode.valueOf("English"))
  }

  @Test
  fun `LanguageMode contains Japanese value`() {
    assertEquals(LanguageMode.Japanese, LanguageMode.valueOf("Japanese"))
  }
}
