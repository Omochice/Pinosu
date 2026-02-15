package io.github.omochice.pinosu.feature.settings.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

  @Test
  fun `enum has System, Light, and Dark values`() {
    val values = ThemeMode.entries
    assertEquals(3, values.size)
    assertEquals(ThemeMode.System, values[0])
    assertEquals(ThemeMode.Light, values[1])
    assertEquals(ThemeMode.Dark, values[2])
  }

  @Test
  fun `System is the first entry for use as default`() {
    assertEquals(ThemeMode.System, ThemeMode.entries.first())
  }
}
