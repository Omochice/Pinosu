package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveClientTagEnabledUseCaseTest {

  @Test
  fun `invoke returns clientTagEnabledFlow from repository`() {
    val settingsRepository: SettingsRepository = mockk(relaxed = true)
    val flow = MutableStateFlow(true)
    every { settingsRepository.clientTagEnabledFlow } returns flow

    val useCase = ObserveClientTagEnabledUseCaseImpl(settingsRepository)

    assertEquals(true, useCase().value)
  }

  @Test
  fun `invoke returns false when repository emits false`() {
    val settingsRepository: SettingsRepository = mockk(relaxed = true)
    val flow = MutableStateFlow(false)
    every { settingsRepository.clientTagEnabledFlow } returns flow

    val useCase = ObserveClientTagEnabledUseCaseImpl(settingsRepository)

    assertEquals(false, useCase().value)
  }
}
