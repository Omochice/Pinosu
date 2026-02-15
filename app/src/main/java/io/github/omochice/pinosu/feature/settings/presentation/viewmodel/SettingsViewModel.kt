package io.github.omochice.pinosu.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveThemeModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetThemeModeUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * ViewModel for Settings screen.
 *
 * Manages settings state and handles settings updates. Observes display mode and theme mode changes
 * for reactive UI updates.
 */
@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    observeDisplayModeUseCase: ObserveDisplayModeUseCase,
    private val setDisplayModeUseCase: SetDisplayModeUseCase,
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(SettingsUiState())

  /** UI state for Settings screen */
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  init {
    observeDisplayModeUseCase()
        .onEach { displayMode -> _uiState.update { it.copy(displayMode = displayMode) } }
        .launchIn(viewModelScope)

    observeThemeModeUseCase()
        .onEach { themeMode -> _uiState.update { it.copy(themeMode = themeMode) } }
        .launchIn(viewModelScope)
  }

  /**
   * Update bookmark display mode preference.
   *
   * @param mode New display mode to save
   */
  fun setDisplayMode(mode: BookmarkDisplayMode) {
    setDisplayModeUseCase(mode)
  }

  /**
   * Update theme mode preference.
   *
   * @param mode New theme mode to save
   */
  fun setThemeMode(mode: ThemeMode) {
    setThemeModeUseCase(mode)
  }
}
