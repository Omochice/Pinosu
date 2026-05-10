package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals

class ObserveLanguageModeUseCaseTest {

  @Test
  fun `invoke returns languageModeFlow from repository`() {
    val settingsRepository: SettingsRepository = mockk(relaxed = true)
    val flow = MutableStateFlow(LanguageMode.English)
    every { settingsRepository.languageModeFlow } returns flow

    val useCase = ObserveLanguageModeUseCaseImpl(settingsRepository)

    assertEquals(LanguageMode.English, useCase().value)
  }

  @Test
  fun `invoke returns System as default from repository`() {
    val settingsRepository: SettingsRepository = mockk(relaxed = true)
    val flow = MutableStateFlow(LanguageMode.System)
    every { settingsRepository.languageModeFlow } returns flow

    val useCase = ObserveLanguageModeUseCaseImpl(settingsRepository)

    assertEquals(LanguageMode.System, useCase().value)
  }
}
