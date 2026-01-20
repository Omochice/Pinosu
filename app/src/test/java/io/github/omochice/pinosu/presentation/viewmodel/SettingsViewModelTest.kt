package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.domain.usecase.GetDisplayModeUseCase
import io.github.omochice.pinosu.domain.usecase.SetDisplayModeUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

  private lateinit var getDisplayModeUseCase: GetDisplayModeUseCase
  private lateinit var setDisplayModeUseCase: SetDisplayModeUseCase
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    getDisplayModeUseCase = mockk()
    setDisplayModeUseCase = mockk(relaxed = true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state loads display mode from use case`() = runTest {
    every { getDisplayModeUseCase() } returns BookmarkDisplayMode.Grid

    val viewModel = SettingsViewModel(getDisplayModeUseCase, setDisplayModeUseCase)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(BookmarkDisplayMode.Grid, state.displayMode)
  }

  @Test
  fun `initial state defaults to List when use case returns List`() = runTest {
    every { getDisplayModeUseCase() } returns BookmarkDisplayMode.List

    val viewModel = SettingsViewModel(getDisplayModeUseCase, setDisplayModeUseCase)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(BookmarkDisplayMode.List, state.displayMode)
  }

  @Test
  fun `setDisplayMode updates state and calls use case`() = runTest {
    every { getDisplayModeUseCase() } returns BookmarkDisplayMode.List

    val viewModel = SettingsViewModel(getDisplayModeUseCase, setDisplayModeUseCase)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.setDisplayMode(BookmarkDisplayMode.Grid)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(BookmarkDisplayMode.Grid, state.displayMode)
    verify { setDisplayModeUseCase(BookmarkDisplayMode.Grid) }
  }

  @Test
  fun `setDisplayMode can change from Grid to List`() = runTest {
    every { getDisplayModeUseCase() } returns BookmarkDisplayMode.Grid

    val viewModel = SettingsViewModel(getDisplayModeUseCase, setDisplayModeUseCase)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.setDisplayMode(BookmarkDisplayMode.List)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(BookmarkDisplayMode.List, state.displayMode)
    verify { setDisplayModeUseCase(BookmarkDisplayMode.List) }
  }
}
