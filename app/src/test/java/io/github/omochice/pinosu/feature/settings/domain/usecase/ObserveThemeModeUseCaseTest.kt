package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveThemeModeUseCaseTest {

  @Test
  fun `invoke returns themeModeFlow from repository`() {
    val settingsRepository: SettingsRepository = mockk(relaxed = true)
    val flow = MutableStateFlow(ThemeMode.Dark)
    every { settingsRepository.themeModeFlow } returns flow

    val useCase = ObserveThemeModeUseCaseImpl(settingsRepository)

    assertEquals(ThemeMode.Dark, useCase().value)
  }

  @Test
  fun `invoke returns System as default from repository`() {
    val settingsRepository: SettingsRepository = mockk(relaxed = true)
    val flow = MutableStateFlow(ThemeMode.System)
    every { settingsRepository.themeModeFlow } returns flow

    val useCase = ObserveThemeModeUseCaseImpl(settingsRepository)

    assertEquals(ThemeMode.System, useCase().value)
  }
}
