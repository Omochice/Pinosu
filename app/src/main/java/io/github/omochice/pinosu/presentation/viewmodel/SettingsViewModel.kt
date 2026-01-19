package io.github.omochice.pinosu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.data.repository.SettingsRepository
import io.github.omochice.pinosu.domain.model.ThemeMode
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for settings screen
 *
 * Manages settings UI state including theme mode selection.
 *
 * @param settingsRepository Repository for accessing settings
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val settingsRepository: SettingsRepository) :
    ViewModel() {

  private val _uiState =
      MutableStateFlow(SettingsUiState(themeMode = settingsRepository.getThemeMode()))
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  /**
   * Update theme mode selection
   *
   * @param mode New theme mode to apply
   */
  fun setThemeMode(mode: ThemeMode) {
    settingsRepository.setThemeMode(mode)
    _uiState.value = _uiState.value.copy(themeMode = mode)
  }
}
