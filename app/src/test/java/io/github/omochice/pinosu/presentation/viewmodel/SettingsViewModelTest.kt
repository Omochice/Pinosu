package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.data.repository.SettingsRepository
import io.github.omochice.pinosu.domain.model.ThemeMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

  private lateinit var settingsRepository: SettingsRepository
  private lateinit var viewModel: SettingsViewModel

  @Before
  fun setup() {
    settingsRepository = mockk()
    every { settingsRepository.getThemeMode() } returns ThemeMode.System
    every { settingsRepository.setThemeMode(any()) } returns Unit
    viewModel = SettingsViewModel(settingsRepository)
  }

  @Test
  fun `initial state is System`() = runTest {
    val state = viewModel.uiState.first()

    assertEquals(ThemeMode.System, state.themeMode)
  }

  @Test
  fun `setThemeMode updates state`() = runTest {
    viewModel.setThemeMode(ThemeMode.Dark)

    val state = viewModel.uiState.first()
    assertEquals(ThemeMode.Dark, state.themeMode)
  }

  @Test
  fun `setThemeMode calls repo`() = runTest {
    viewModel.setThemeMode(ThemeMode.Light)

    verify { settingsRepository.setThemeMode(ThemeMode.Light) }
  }

  @Test
  fun `all modes update correctly`() = runTest {
    viewModel.setThemeMode(ThemeMode.Light)
    assertEquals(ThemeMode.Light, viewModel.uiState.first().themeMode)

    viewModel.setThemeMode(ThemeMode.Dark)
    assertEquals(ThemeMode.Dark, viewModel.uiState.first().themeMode)

    viewModel.setThemeMode(ThemeMode.System)
    assertEquals(ThemeMode.System, viewModel.uiState.first().themeMode)
  }
}
