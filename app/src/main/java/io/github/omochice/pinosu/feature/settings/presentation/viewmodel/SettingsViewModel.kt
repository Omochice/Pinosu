package io.github.omochice.pinosu.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale
import io.github.omochice.pinosu.feature.settings.domain.usecase.GetCurrentLocaleUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetLocaleUseCase
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
 * Manages settings state and handles settings updates. Observes display mode changes for reactive
 * UI updates.
 */
@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    observeDisplayModeUseCase: ObserveDisplayModeUseCase,
    private val setDisplayModeUseCase: SetDisplayModeUseCase,
    getCurrentLocaleUseCase: GetCurrentLocaleUseCase,
    private val setLocaleUseCase: SetLocaleUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(SettingsUiState())

  /** UI state for Settings screen */
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  init {
    observeDisplayModeUseCase()
        .onEach { displayMode -> _uiState.update { it.copy(displayMode = displayMode) } }
        .launchIn(viewModelScope)

    _uiState.update { it.copy(locale = getCurrentLocaleUseCase()) }
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
   * Update application locale preference.
   *
   * @param locale New [AppLocale] to apply
   */
  fun setLocale(locale: AppLocale) {
    setLocaleUseCase(locale)
    _uiState.update { it.copy(locale = locale) }
  }
}
