package io.github.omochice.pinosu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.domain.usecase.GetDisplayModeUseCase
import io.github.omochice.pinosu.domain.usecase.SetDisplayModeUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for Settings screen.
 *
 * Manages settings state and handles settings updates.
 *
 * @property getDisplayModeUseCase Use case for retrieving display mode
 * @property setDisplayModeUseCase Use case for saving display mode
 */
@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    private val getDisplayModeUseCase: GetDisplayModeUseCase,
    private val setDisplayModeUseCase: SetDisplayModeUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(SettingsUiState())

  /** UI state for Settings screen */
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  init {
    loadSettings()
  }

  private fun loadSettings() {
    _uiState.update { it.copy(displayMode = getDisplayModeUseCase()) }
  }

  /**
   * Update bookmark display mode preference.
   *
   * @param mode New display mode to save
   */
  fun setDisplayMode(mode: BookmarkDisplayMode) {
    setDisplayModeUseCase(mode)
    _uiState.update { it.copy(displayMode = mode) }
  }
}
