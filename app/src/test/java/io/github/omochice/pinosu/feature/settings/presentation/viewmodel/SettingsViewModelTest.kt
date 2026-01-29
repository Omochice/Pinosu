package io.github.omochice.pinosu.feature.settings.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetDisplayModeUseCase
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
  private lateinit var displayModeFlow: MutableStateFlow<BookmarkDisplayMode>
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    displayModeFlow = MutableStateFlow(BookmarkDisplayMode.List)
    observeDisplayModeUseCase = mockk()
    every { observeDisplayModeUseCase() } returns displayModeFlow
    setDisplayModeUseCase = mockk(relaxed = true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state loads display mode from observed flow`() = runTest {
    displayModeFlow.value = BookmarkDisplayMode.Grid

    val viewModel = SettingsViewModel(observeDisplayModeUseCase, setDisplayModeUseCase)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(BookmarkDisplayMode.Grid, state.displayMode)
  }

  @Test
  fun `initial state defaults to List when flow emits List`() = runTest {
    displayModeFlow.value = BookmarkDisplayMode.List

    val viewModel = SettingsViewModel(observeDisplayModeUseCase, setDisplayModeUseCase)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(BookmarkDisplayMode.List, state.displayMode)
  }

  @Test
  fun `setDisplayMode calls use case`() = runTest {
    val viewModel = SettingsViewModel(observeDisplayModeUseCase, setDisplayModeUseCase)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.setDisplayMode(BookmarkDisplayMode.Grid)

    verify { setDisplayModeUseCase(BookmarkDisplayMode.Grid) }
  }

  @Test
  fun `state updates when observed flow emits new value`() = runTest {
    val viewModel = SettingsViewModel(observeDisplayModeUseCase, setDisplayModeUseCase)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(BookmarkDisplayMode.List, viewModel.uiState.first().displayMode)

    displayModeFlow.value = BookmarkDisplayMode.Grid
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(BookmarkDisplayMode.Grid, viewModel.uiState.first().displayMode)
  }
}
