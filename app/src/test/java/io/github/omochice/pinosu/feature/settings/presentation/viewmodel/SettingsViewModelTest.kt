package io.github.omochice.pinosu.feature.settings.presentation.viewmodel

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveThemeModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetThemeModeUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

  private lateinit var observeDisplayModeUseCase: ObserveDisplayModeUseCase
  private lateinit var setDisplayModeUseCase: SetDisplayModeUseCase
  private lateinit var observeThemeModeUseCase: ObserveThemeModeUseCase
  private lateinit var setThemeModeUseCase: SetThemeModeUseCase
  private lateinit var displayModeFlow: MutableStateFlow<BookmarkDisplayMode>
  private lateinit var themeModeFlow: MutableStateFlow<ThemeMode>
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    displayModeFlow = MutableStateFlow(BookmarkDisplayMode.List)
    themeModeFlow = MutableStateFlow(ThemeMode.System)
    observeDisplayModeUseCase = mockk()
    every { observeDisplayModeUseCase() } returns displayModeFlow
    setDisplayModeUseCase = mockk(relaxed = true)
    observeThemeModeUseCase = mockk()
    every { observeThemeModeUseCase() } returns themeModeFlow
    setThemeModeUseCase = mockk(relaxed = true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel() =
      SettingsViewModel(
          observeDisplayModeUseCase,
          setDisplayModeUseCase,
          observeThemeModeUseCase,
          setThemeModeUseCase)

  @Test
  fun `initial state loads display mode from observed flow`() = runTest {
    displayModeFlow.value = BookmarkDisplayMode.Grid

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(BookmarkDisplayMode.Grid, state.displayMode)
  }

  @Test
  fun `initial state defaults to List when flow emits List`() = runTest {
    displayModeFlow.value = BookmarkDisplayMode.List

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(BookmarkDisplayMode.List, state.displayMode)
  }

  @Test
  fun `setDisplayMode calls use case`() = runTest {
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.setDisplayMode(BookmarkDisplayMode.Grid)

    verify { setDisplayModeUseCase(BookmarkDisplayMode.Grid) }
  }

  @Test
  fun `display mode state updates when observed flow emits new value`() = runTest {
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(BookmarkDisplayMode.List, viewModel.uiState.first().displayMode)

    displayModeFlow.value = BookmarkDisplayMode.Grid
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(BookmarkDisplayMode.Grid, viewModel.uiState.first().displayMode)
  }

  @Test
  fun `initial state loads theme mode from observed flow`() = runTest {
    themeModeFlow.value = ThemeMode.Dark

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(ThemeMode.Dark, state.themeMode)
  }

  @Test
  fun `initial state defaults to System when theme mode flow emits System`() = runTest {
    themeModeFlow.value = ThemeMode.System

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(ThemeMode.System, state.themeMode)
  }

  @Test
  fun `setThemeMode calls use case`() = runTest {
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.setThemeMode(ThemeMode.Dark)

    verify { setThemeModeUseCase(ThemeMode.Dark) }
  }

  @Test
  fun `theme mode state updates when observed flow emits new value`() = runTest {
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(ThemeMode.System, viewModel.uiState.first().themeMode)

    themeModeFlow.value = ThemeMode.Dark
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(ThemeMode.Dark, viewModel.uiState.first().themeMode)
  }
}
