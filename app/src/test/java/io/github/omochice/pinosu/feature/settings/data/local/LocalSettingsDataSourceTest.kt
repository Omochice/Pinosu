package io.github.omochice.pinosu.feature.settings.data.local

import android.content.Context
import android.content.SharedPreferences
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LocalSettingsDataSourceTest {

  private lateinit var context: Context
  private lateinit var sharedPreferences: SharedPreferences
  private lateinit var editor: SharedPreferences.Editor
  private lateinit var dataSource: LocalSettingsDataSource

  @Before
  fun setup() {
    context = mockk(relaxed = true)
    sharedPreferences = mockk(relaxed = true)
    editor = mockk(relaxed = true)

    every { context.getSharedPreferences("pinosu_settings", Context.MODE_PRIVATE) } returns
        sharedPreferences
    every { sharedPreferences.edit() } returns editor
    every { editor.putString(any(), any()) } returns editor

    dataSource = LocalSettingsDataSource(context)
  }

  @Test
  fun `getDisplayMode returns List when no value is stored`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_DISPLAY_MODE, null) } returns
        null

    val result = dataSource.getDisplayMode()

    assertEquals(BookmarkDisplayMode.List, result)
  }

  @Test
  fun `getDisplayMode returns List when stored value is List`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_DISPLAY_MODE, null) } returns
        "List"

    val result = dataSource.getDisplayMode()

    assertEquals(BookmarkDisplayMode.List, result)
  }

  @Test
  fun `getDisplayMode returns Grid when stored value is Grid`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_DISPLAY_MODE, null) } returns
        "Grid"

    val result = dataSource.getDisplayMode()

    assertEquals(BookmarkDisplayMode.Grid, result)
  }

  @Test
  fun `getDisplayMode returns List when stored value is invalid`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_DISPLAY_MODE, null) } returns
        "InvalidValue"

    val result = dataSource.getDisplayMode()

    assertEquals(BookmarkDisplayMode.List, result)
  }

  @Test
  fun `setDisplayMode saves List value`() {
    dataSource.setDisplayMode(BookmarkDisplayMode.List)

    verify { editor.putString(LocalSettingsDataSource.KEY_DISPLAY_MODE, "List") }
    verify { editor.apply() }
  }

  @Test
  fun `setDisplayMode saves Grid value`() {
    dataSource.setDisplayMode(BookmarkDisplayMode.Grid)

    verify { editor.putString(LocalSettingsDataSource.KEY_DISPLAY_MODE, "Grid") }
    verify { editor.apply() }
  }

  @Test
  fun `setDisplayMode updates displayModeFlow with new value`() {
    dataSource.setDisplayMode(BookmarkDisplayMode.Grid)

    assertEquals(BookmarkDisplayMode.Grid, dataSource.displayModeFlow.value)
  }

  @Test
  fun `displayModeFlow is initialized with stored value on construction`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_DISPLAY_MODE, null) } returns
        "Grid"

    val newDataSource = LocalSettingsDataSource(context)

    assertEquals(BookmarkDisplayMode.Grid, newDataSource.displayModeFlow.value)
  }

  @Test
  fun `getThemeMode returns System when no value is stored`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_THEME_MODE, null) } returns null

    val result = dataSource.getThemeMode()

    assertEquals(ThemeMode.System, result)
  }

  @Test
  fun `getThemeMode returns Light when stored value is Light`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_THEME_MODE, null) } returns
        "Light"

    val result = dataSource.getThemeMode()

    assertEquals(ThemeMode.Light, result)
  }

  @Test
  fun `getThemeMode returns Dark when stored value is Dark`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_THEME_MODE, null) } returns
        "Dark"

    val result = dataSource.getThemeMode()

    assertEquals(ThemeMode.Dark, result)
  }

  @Test
  fun `getThemeMode returns System when stored value is invalid`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_THEME_MODE, null) } returns
        "InvalidValue"

    val result = dataSource.getThemeMode()

    assertEquals(ThemeMode.System, result)
  }

  @Test
  fun `setThemeMode saves Light value`() {
    dataSource.setThemeMode(ThemeMode.Light)

    verify { editor.putString(LocalSettingsDataSource.KEY_THEME_MODE, "Light") }
    verify { editor.apply() }
  }

  @Test
  fun `setThemeMode saves Dark value`() {
    dataSource.setThemeMode(ThemeMode.Dark)

    verify { editor.putString(LocalSettingsDataSource.KEY_THEME_MODE, "Dark") }
    verify { editor.apply() }
  }

  @Test
  fun `setThemeMode updates themeModeFlow with new value`() {
    dataSource.setThemeMode(ThemeMode.Dark)

    assertEquals(ThemeMode.Dark, dataSource.themeModeFlow.value)
  }

  @Test
  fun `themeModeFlow is initialized with stored value on construction`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_THEME_MODE, null) } returns
        "Dark"

    val newDataSource = LocalSettingsDataSource(context)

    assertEquals(ThemeMode.Dark, newDataSource.themeModeFlow.value)
  }

  @Test
  fun `getLanguageMode returns System when no value is stored`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_LANGUAGE_MODE, null) } returns
        null

    val result = dataSource.getLanguageMode()

    assertEquals(LanguageMode.System, result)
  }

  @Test
  fun `getLanguageMode returns English when stored value is English`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_LANGUAGE_MODE, null) } returns
        "English"

    val result = dataSource.getLanguageMode()

    assertEquals(LanguageMode.English, result)
  }

  @Test
  fun `getLanguageMode returns Japanese when stored value is Japanese`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_LANGUAGE_MODE, null) } returns
        "Japanese"

    val result = dataSource.getLanguageMode()

    assertEquals(LanguageMode.Japanese, result)
  }

  @Test
  fun `getLanguageMode returns System when stored value is invalid`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_LANGUAGE_MODE, null) } returns
        "InvalidValue"

    val result = dataSource.getLanguageMode()

    assertEquals(LanguageMode.System, result)
  }

  @Test
  fun `setLanguageMode saves English value`() {
    dataSource.setLanguageMode(LanguageMode.English)

    verify { editor.putString(LocalSettingsDataSource.KEY_LANGUAGE_MODE, "English") }
    verify { editor.apply() }
  }

  @Test
  fun `setLanguageMode saves Japanese value`() {
    dataSource.setLanguageMode(LanguageMode.Japanese)

    verify { editor.putString(LocalSettingsDataSource.KEY_LANGUAGE_MODE, "Japanese") }
    verify { editor.apply() }
  }

  @Test
  fun `setLanguageMode updates languageModeFlow with new value`() {
    dataSource.setLanguageMode(LanguageMode.Japanese)

    assertEquals(LanguageMode.Japanese, dataSource.languageModeFlow.value)
  }

  @Test
  fun `languageModeFlow is initialized with stored value on construction`() {
    every { sharedPreferences.getString(LocalSettingsDataSource.KEY_LANGUAGE_MODE, null) } returns
        "Japanese"

    val newDataSource = LocalSettingsDataSource(context)

    assertEquals(LanguageMode.Japanese, newDataSource.languageModeFlow.value)
  }

  @Test
  fun `getClientTagEnabled returns true when no value is stored`() {
    every {
      sharedPreferences.getBoolean(LocalSettingsDataSource.KEY_CLIENT_TAG_ENABLED, true)
    } returns true

    val result = dataSource.getClientTagEnabled()

    assertEquals(true, result)
  }

  @Test
  fun `getClientTagEnabled returns false when stored value is false`() {
    every {
      sharedPreferences.getBoolean(LocalSettingsDataSource.KEY_CLIENT_TAG_ENABLED, true)
    } returns false

    val result = dataSource.getClientTagEnabled()

    assertEquals(false, result)
  }

  @Test
  fun `setClientTagEnabled saves false value`() {
    dataSource.setClientTagEnabled(false)

    verify { editor.putBoolean(LocalSettingsDataSource.KEY_CLIENT_TAG_ENABLED, false) }
    verify { editor.apply() }
  }

  @Test
  fun `setClientTagEnabled updates clientTagEnabledFlow with new value`() {
    dataSource.setClientTagEnabled(false)

    assertEquals(false, dataSource.clientTagEnabledFlow.value)
  }

  @Test
  fun `clientTagEnabledFlow is initialized with stored value on construction`() {
    every {
      sharedPreferences.getBoolean(LocalSettingsDataSource.KEY_CLIENT_TAG_ENABLED, true)
    } returns false

    val newDataSource = LocalSettingsDataSource(context)

    assertEquals(false, newDataSource.clientTagEnabledFlow.value)
  }
}
