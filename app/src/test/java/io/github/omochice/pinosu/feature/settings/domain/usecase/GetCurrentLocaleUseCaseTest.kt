package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.data.repository.SettingsRepository
import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCurrentLocaleUseCaseTest {

  private lateinit var settingsRepository: SettingsRepository
  private lateinit var useCase: GetCurrentLocaleUseCase

  @Before
  fun setup() {
    settingsRepository = mockk(relaxed = true)
    useCase = GetCurrentLocaleUseCaseImpl(settingsRepository)
  }

  @Test
  fun `invoke returns System when repository returns System`() {
    every { settingsRepository.getLocale() } returns AppLocale.System

    assertEquals(AppLocale.System, useCase())
  }

  @Test
  fun `invoke returns English when repository returns English`() {
    every { settingsRepository.getLocale() } returns AppLocale.English

    assertEquals(AppLocale.English, useCase())
  }

  @Test
  fun `invoke returns Japanese when repository returns Japanese`() {
    every { settingsRepository.getLocale() } returns AppLocale.Japanese

    assertEquals(AppLocale.Japanese, useCase())
  }
}
