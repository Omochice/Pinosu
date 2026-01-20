package io.github.omochice.pinosu.data.local

import android.content.Context
import android.content.SharedPreferences
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
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
}
