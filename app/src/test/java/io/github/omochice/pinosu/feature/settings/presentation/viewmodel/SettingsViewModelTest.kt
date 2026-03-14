package io.github.omochice.pinosu.feature.settings.presentation.viewmodel

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveBootstrapRelaysUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveLanguageModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveThemeModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetBootstrapRelaysUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetLanguageModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetThemeModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SettingsUseCases
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
  private lateinit var observeLanguageModeUseCase: ObserveLanguageModeUseCase
  private lateinit var setLanguageModeUseCase: SetLanguageModeUseCase
  private lateinit var observeBootstrapRelaysUseCase: ObserveBootstrapRelaysUseCase
  private lateinit var setBootstrapRelaysUseCase: SetBootstrapRelaysUseCase
  private lateinit var displayModeFlow: MutableStateFlow<BookmarkDisplayMode>
  private lateinit var themeModeFlow: MutableStateFlow<ThemeMode>
  private lateinit var languageModeFlow: MutableStateFlow<LanguageMode>
  private lateinit var bootstrapRelaysFlow: MutableStateFlow<Set<String>>
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    displayModeFlow = MutableStateFlow(BookmarkDisplayMode.List)
    themeModeFlow = MutableStateFlow(ThemeMode.System)
    languageModeFlow = MutableStateFlow(LanguageMode.System)
    bootstrapRelaysFlow = MutableStateFlow(emptySet())
    observeDisplayModeUseCase = mockk()
    every { observeDisplayModeUseCase() } returns displayModeFlow
    setDisplayModeUseCase = mockk(relaxed = true)
    observeThemeModeUseCase = mockk()
    every { observeThemeModeUseCase() } returns themeModeFlow
    setThemeModeUseCase = mockk(relaxed = true)
    observeLanguageModeUseCase = mockk()
    every { observeLanguageModeUseCase() } returns languageModeFlow
    setLanguageModeUseCase = mockk(relaxed = true)
    observeBootstrapRelaysUseCase = mockk()
    every { observeBootstrapRelaysUseCase() } returns bootstrapRelaysFlow
    setBootstrapRelaysUseCase = mockk(relaxed = true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel() =
      SettingsViewModel(
          SettingsUseCases(
              observeDisplayMode = observeDisplayModeUseCase,
              setDisplayMode = setDisplayModeUseCase,
              observeThemeMode = observeThemeModeUseCase,
              setThemeMode = setThemeModeUseCase,
              observeLanguageMode = observeLanguageModeUseCase,
              setLanguageMode = setLanguageModeUseCase,
              observeBootstrapRelays = observeBootstrapRelaysUseCase,
              setBootstrapRelays = setBootstrapRelaysUseCase,
          ))

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

  @Test
  fun `initial state loads language mode from observed flow`() = runTest {
    languageModeFlow.value = LanguageMode.English

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(LanguageMode.English, state.languageMode)
  }

  @Test
  fun `initial state defaults to System when language mode flow emits System`() = runTest {
    languageModeFlow.value = LanguageMode.System

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(LanguageMode.System, state.languageMode)
  }

  @Test
  fun `setLanguageMode calls use case`() = runTest {
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.setLanguageMode(LanguageMode.Japanese)

    verify { setLanguageModeUseCase(LanguageMode.Japanese) }
  }

  @Test
  fun `language mode state updates when observed flow emits new value`() = runTest {
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(LanguageMode.System, viewModel.uiState.first().languageMode)

    languageModeFlow.value = LanguageMode.Japanese
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(LanguageMode.Japanese, viewModel.uiState.first().languageMode)
  }

  @Test
  fun `addBootstrapRelay adds relay URL via use case`() = runTest {
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.addBootstrapRelay("wss://custom-relay.example.com")

    verify { setBootstrapRelaysUseCase(setOf("wss://custom-relay.example.com")) }
  }

  @Test
  fun `removeBootstrapRelay removes relay URL via use case`() = runTest {
    bootstrapRelaysFlow.value = setOf("wss://relay1.example.com", "wss://relay2.example.com")
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.removeBootstrapRelay("wss://relay1.example.com")

    verify { setBootstrapRelaysUseCase(setOf("wss://relay2.example.com")) }
  }

  @Test
  fun `bootstrap relays state updates when observed flow emits new value`() = runTest {
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(emptySet<String>(), viewModel.uiState.first().bootstrapRelays)

    bootstrapRelaysFlow.value = setOf("wss://relay.example.com")
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(setOf("wss://relay.example.com"), viewModel.uiState.first().bootstrapRelays)
  }

  @Test
  fun `resetBootstrapRelays sets default relay URLs via use case`() = runTest {
    bootstrapRelaysFlow.value = setOf("wss://custom-relay.example.com")
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.resetBootstrapRelays()

    verify {
      setBootstrapRelaysUseCase(
          io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcherImpl
              .DEFAULT_BOOTSTRAP_RELAY_URLS)
    }
  }
}
