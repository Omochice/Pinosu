package io.github.omochice.pinosu.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcherImpl
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.github.omochice.pinosu.feature.settings.domain.usecase.SettingsUseCases
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
 * Manages settings state and handles settings updates. Observes display mode, theme mode, language
 * mode, and bootstrap relay changes for reactive UI updates.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val useCases: SettingsUseCases) : ViewModel() {

  private val _uiState = MutableStateFlow(SettingsUiState())

  /** UI state for Settings screen */
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  init {
    useCases
        .observeDisplayMode()
        .onEach { displayMode -> _uiState.update { it.copy(displayMode = displayMode) } }
        .launchIn(viewModelScope)

    useCases
        .observeThemeMode()
        .onEach { themeMode -> _uiState.update { it.copy(themeMode = themeMode) } }
        .launchIn(viewModelScope)

    useCases
        .observeLanguageMode()
        .onEach { languageMode -> _uiState.update { it.copy(languageMode = languageMode) } }
        .launchIn(viewModelScope)

    useCases
        .observeBootstrapRelays()
        .onEach { relays -> _uiState.update { it.copy(bootstrapRelays = relays) } }
        .launchIn(viewModelScope)
  }

  /**
   * Update bookmark display mode preference.
   *
   * @param mode New display mode to save
   */
  fun setDisplayMode(mode: BookmarkDisplayMode) {
    useCases.setDisplayMode(mode)
  }

  /**
   * Update theme mode preference.
   *
   * @param mode New theme mode to save
   */
  fun setThemeMode(mode: ThemeMode) {
    useCases.setThemeMode(mode)
  }

  /**
   * Update language mode preference.
   *
   * @param mode New language mode to save
   */
  fun setLanguageMode(mode: LanguageMode) {
    useCases.setLanguageMode(mode)
  }

  /**
   * Add a bootstrap relay URL to the user-configured set.
   *
   * @param url Relay URL to add
   */
  fun addBootstrapRelay(url: String) {
    val current = _uiState.value.bootstrapRelays
    useCases.setBootstrapRelays(current + url)
  }

  /**
   * Remove a bootstrap relay URL from the user-configured set.
   *
   * @param url Relay URL to remove
   */
  fun removeBootstrapRelay(url: String) {
    val current = _uiState.value.bootstrapRelays
    useCases.setBootstrapRelays(current - url)
  }

  /** Reset bootstrap relays to the default set. */
  fun resetBootstrapRelays() {
    useCases.setBootstrapRelays(Nip65RelayListFetcherImpl.DEFAULT_BOOTSTRAP_RELAY_URLS)
  }
}
