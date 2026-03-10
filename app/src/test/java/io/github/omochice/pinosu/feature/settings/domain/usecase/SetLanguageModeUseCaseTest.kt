package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetLanguageModeUseCaseTest {

  private lateinit var settingsRepository: SettingsRepository
  private lateinit var useCase: SetLanguageModeUseCase

  @Before
  fun setup() {
    settingsRepository = mockk(relaxed = true)
    useCase = SetLanguageModeUseCaseImpl(settingsRepository)
  }

  @Test
  fun `invoke saves System mode to repository`() {
    useCase(LanguageMode.System)

    verify { settingsRepository.setLanguageMode(LanguageMode.System) }
  }

  @Test
  fun `invoke saves English mode to repository`() {
    useCase(LanguageMode.English)

    verify { settingsRepository.setLanguageMode(LanguageMode.English) }
  }

  @Test
  fun `invoke saves Japanese mode to repository`() {
    useCase(LanguageMode.Japanese)

    verify { settingsRepository.setLanguageMode(LanguageMode.Japanese) }
  }
}
