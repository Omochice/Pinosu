package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.data.repository.SettingsRepository
import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetLocaleUseCaseTest {

  private lateinit var settingsRepository: SettingsRepository
  private lateinit var useCase: SetLocaleUseCase

  @Before
  fun setup() {
    settingsRepository = mockk(relaxed = true)
    useCase = SetLocaleUseCaseImpl(settingsRepository)
  }

  @Test
  fun `invoke sets System locale on repository`() {
    useCase(AppLocale.System)

    verify { settingsRepository.setLocale(AppLocale.System) }
  }

  @Test
  fun `invoke sets English locale on repository`() {
    useCase(AppLocale.English)

    verify { settingsRepository.setLocale(AppLocale.English) }
  }

  @Test
  fun `invoke sets Japanese locale on repository`() {
    useCase(AppLocale.Japanese)

    verify { settingsRepository.setLocale(AppLocale.Japanese) }
  }
}
