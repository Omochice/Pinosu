package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.data.repository.SettingsRepository
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetThemeModeUseCaseTest {

  private lateinit var settingsRepository: SettingsRepository
  private lateinit var useCase: SetThemeModeUseCase

  @Before
  fun setup() {
    settingsRepository = mockk(relaxed = true)
    useCase = SetThemeModeUseCaseImpl(settingsRepository)
  }

  @Test
  fun `invoke saves System mode to repository`() {
    useCase(ThemeMode.System)

    verify { settingsRepository.setThemeMode(ThemeMode.System) }
  }

  @Test
  fun `invoke saves Light mode to repository`() {
    useCase(ThemeMode.Light)

    verify { settingsRepository.setThemeMode(ThemeMode.Light) }
  }

  @Test
  fun `invoke saves Dark mode to repository`() {
    useCase(ThemeMode.Dark)

    verify { settingsRepository.setThemeMode(ThemeMode.Dark) }
  }
}
