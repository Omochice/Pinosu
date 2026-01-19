package io.github.omochice.pinosu.data.repository

import android.content.Context
import io.github.omochice.pinosu.data.local.LocalSettingsDataSource
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
class SettingsRepositoryImplTest {

  private lateinit var sharedPreferences: android.content.SharedPreferences
  private lateinit var localSettingsDataSource: LocalSettingsDataSource
  private lateinit var settingsRepository: SettingsRepository

  @Before
  fun setup() {
    val context = RuntimeEnvironment.getApplication()
    sharedPreferences = context.getSharedPreferences("test_settings", Context.MODE_PRIVATE)
    sharedPreferences.edit().clear().commit()

    localSettingsDataSource = LocalSettingsDataSource(context)
    localSettingsDataSource.setTestSharedPreferences(sharedPreferences)

    settingsRepository = SettingsRepositoryImpl(localSettingsDataSource)
  }

  @Test
  fun `themeMode StateFlow emits System as initial value`() {
    val result = settingsRepository.themeMode.value

    assertEquals(ThemeMode.System, result)
  }

  @Test
  fun `getThemeMode returns System as default`() {
    val result = settingsRepository.getThemeMode()

    assertEquals(ThemeMode.System, result)
  }

  @Test
  fun `setThemeMode updates themeMode StateFlow`() {
    settingsRepository.setThemeMode(ThemeMode.Dark)

    assertEquals(ThemeMode.Dark, settingsRepository.themeMode.value)
  }

  @Test
  fun `setThemeMode persists to LocalSettingsDataSource`() {
    settingsRepository.setThemeMode(ThemeMode.Light)

    assertEquals(ThemeMode.Light, localSettingsDataSource.getThemeMode())
  }

  @Test
  fun `getThemeMode returns value from StateFlow`() {
    settingsRepository.setThemeMode(ThemeMode.Dark)

    assertEquals(ThemeMode.Dark, settingsRepository.getThemeMode())
  }
}
