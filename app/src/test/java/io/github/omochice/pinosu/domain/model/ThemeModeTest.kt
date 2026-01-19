package io.github.omochice.pinosu.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

  @Test
  fun `has exactly three entries`() {
    assertEquals(3, ThemeMode.entries.size)
  }

  @Test
  fun `entries order is correct`() {
    val entries = ThemeMode.entries
    assertEquals(ThemeMode.Light, entries[0])
    assertEquals(ThemeMode.Dark, entries[1])
    assertEquals(ThemeMode.System, entries[2])
  }
}
