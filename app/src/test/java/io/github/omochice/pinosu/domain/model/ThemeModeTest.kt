package io.github.omochice.pinosu.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

  @Test
  fun `ThemeMode has exactly three entries`() {
    assertEquals(3, ThemeMode.entries.size)
  }

  @Test
  fun `ThemeMode entries are Light, Dark, System in order`() {
    val entries = ThemeMode.entries
    assertEquals(ThemeMode.Light, entries[0])
    assertEquals(ThemeMode.Dark, entries[1])
    assertEquals(ThemeMode.System, entries[2])
  }
}
