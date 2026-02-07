package io.github.omochice.pinosu.feature.settings.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLocaleTest {

  @Test
  fun `System has empty tag`() {
    assertEquals("", AppLocale.System.tag)
  }

  @Test
  fun `English has en tag`() {
    assertEquals("en", AppLocale.English.tag)
  }

  @Test
  fun `Japanese has ja tag`() {
    assertEquals("ja", AppLocale.Japanese.tag)
  }

  @Test
  fun `fromTag returns System for empty string`() {
    assertEquals(AppLocale.System, AppLocale.fromTag(""))
  }

  @Test
  fun `fromTag returns System for null`() {
    assertEquals(AppLocale.System, AppLocale.fromTag(null))
  }

  @Test
  fun `fromTag returns English for en`() {
    assertEquals(AppLocale.English, AppLocale.fromTag("en"))
  }

  @Test
  fun `fromTag returns Japanese for ja`() {
    assertEquals(AppLocale.Japanese, AppLocale.fromTag("ja"))
  }

  @Test
  fun `fromTag returns System for unknown tag`() {
    assertEquals(AppLocale.System, AppLocale.fromTag("fr"))
  }
}
