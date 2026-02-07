package io.github.omochice.pinosu.feature.settings.data.local

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
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

    mockkStatic(AppCompatDelegate::class)
    every { AppCompatDelegate.getApplicationLocales() } returns
        LocaleListCompat.getEmptyLocaleList()

    dataSource = LocalSettingsDataSource(context)
  }

  @After
  fun tearDown() {
    unmockkStatic(AppCompatDelegate::class)
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
  fun `getLocale returns System when no locale is set`() {
    every { AppCompatDelegate.getApplicationLocales() } returns
        LocaleListCompat.getEmptyLocaleList()

    assertEquals(AppLocale.System, dataSource.getLocale())
  }

  @Test
  fun `getLocale returns English when en locale is set`() {
    every { AppCompatDelegate.getApplicationLocales() } returns
        LocaleListCompat.create(Locale.ENGLISH)

    assertEquals(AppLocale.English, dataSource.getLocale())
  }

  @Test
  fun `getLocale returns Japanese when ja locale is set`() {
    every { AppCompatDelegate.getApplicationLocales() } returns
        LocaleListCompat.create(Locale.JAPANESE)

    assertEquals(AppLocale.Japanese, dataSource.getLocale())
  }

  @Test
  fun `setLocale calls AppCompatDelegate with correct locale for English`() {
    dataSource.setLocale(AppLocale.English)

    verify {
      AppCompatDelegate.setApplicationLocales(match { !it.isEmpty && it[0]?.language == "en" })
    }
  }

  @Test
  fun `setLocale calls AppCompatDelegate with correct locale for Japanese`() {
    dataSource.setLocale(AppLocale.Japanese)

    verify {
      AppCompatDelegate.setApplicationLocales(match { !it.isEmpty && it[0]?.language == "ja" })
    }
  }

  @Test
  fun `setLocale calls AppCompatDelegate with empty locale list for System`() {
    dataSource.setLocale(AppLocale.System)

    verify { AppCompatDelegate.setApplicationLocales(match { it.isEmpty }) }
  }

  @Config(sdk = [33])
  @Test
  fun `getLocale returns System when LocaleManager has empty locales on Api33`() {
    val localeManager = mockk<LocaleManager>()
    every { context.getSystemService(LocaleManager::class.java) } returns localeManager
    every { localeManager.applicationLocales } returns LocaleList.getEmptyLocaleList()

    assertEquals(AppLocale.System, dataSource.getLocale())
  }

  @Config(sdk = [33])
  @Test
  fun `getLocale returns English from LocaleManager on Api33`() {
    val localeManager = mockk<LocaleManager>()
    every { context.getSystemService(LocaleManager::class.java) } returns localeManager
    every { localeManager.applicationLocales } returns LocaleList(Locale.ENGLISH)

    assertEquals(AppLocale.English, dataSource.getLocale())
  }

  @Config(sdk = [33])
  @Test
  fun `getLocale returns Japanese from LocaleManager on Api33`() {
    val localeManager = mockk<LocaleManager>()
    every { context.getSystemService(LocaleManager::class.java) } returns localeManager
    every { localeManager.applicationLocales } returns LocaleList(Locale.JAPANESE)

    assertEquals(AppLocale.Japanese, dataSource.getLocale())
  }

  @Config(sdk = [33])
  @Test
  fun `setLocale calls LocaleManager with English on Api33`() {
    val localeManager = mockk<LocaleManager>(relaxed = true)
    every { context.getSystemService(LocaleManager::class.java) } returns localeManager

    dataSource.setLocale(AppLocale.English)

    verify { localeManager.applicationLocales = match { !it.isEmpty && it[0].language == "en" } }
  }

  @Config(sdk = [33])
  @Test
  fun `setLocale calls LocaleManager with Japanese on Api33`() {
    val localeManager = mockk<LocaleManager>(relaxed = true)
    every { context.getSystemService(LocaleManager::class.java) } returns localeManager

    dataSource.setLocale(AppLocale.Japanese)

    verify { localeManager.applicationLocales = match { !it.isEmpty && it[0].language == "ja" } }
  }

  @Config(sdk = [33])
  @Test
  fun `setLocale calls LocaleManager with empty list for System on Api33`() {
    val localeManager = mockk<LocaleManager>(relaxed = true)
    every { context.getSystemService(LocaleManager::class.java) } returns localeManager

    dataSource.setLocale(AppLocale.System)

    verify { localeManager.applicationLocales = match { it.isEmpty } }
  }
}
