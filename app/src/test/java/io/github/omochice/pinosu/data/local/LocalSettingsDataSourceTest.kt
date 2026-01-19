package io.github.omochice.pinosu.data.local

import android.content.Context
import io.github.omochice.pinosu.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LocalSettingsDataSourceTest {

  private lateinit var sharedPreferences: android.content.SharedPreferences
  private lateinit var localSettingsDataSource: LocalSettingsDataSource

  @Before
  fun setup() {
    val context = RuntimeEnvironment.getApplication()
    sharedPreferences = context.getSharedPreferences("test_settings", Context.MODE_PRIVATE)
    sharedPreferences.edit().clear().commit()

    localSettingsDataSource = LocalSettingsDataSource(context)
    localSettingsDataSource.setTestSharedPreferences(sharedPreferences)
  }

  @Test
  fun `default is System`() {
    val result = localSettingsDataSource.getThemeMode()

    assertEquals(ThemeMode.System, result)
  }

  @Test
  fun `persists Light`() {
    localSettingsDataSource.setThemeMode(ThemeMode.Light)

    val result = localSettingsDataSource.getThemeMode()

    assertEquals(ThemeMode.Light, result)
  }

  @Test
  fun `persists Dark`() {
    localSettingsDataSource.setThemeMode(ThemeMode.Dark)

    val result = localSettingsDataSource.getThemeMode()

    assertEquals(ThemeMode.Dark, result)
  }

  @Test
  fun `persists System`() {
    localSettingsDataSource.setThemeMode(ThemeMode.Light)
    localSettingsDataSource.setThemeMode(ThemeMode.System)

    val result = localSettingsDataSource.getThemeMode()

    assertEquals(ThemeMode.System, result)
  }

  @Test
  fun `invalid value returns System`() {
    sharedPreferences.edit().putInt("theme_mode", 999).commit()

    val result = localSettingsDataSource.getThemeMode()

    assertEquals(ThemeMode.System, result)
  }
}
