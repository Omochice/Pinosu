package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetClientTagEnabledUseCaseTest {

  private lateinit var settingsRepository: SettingsRepository
  private lateinit var useCase: SetClientTagEnabledUseCase

  @Before
  fun setup() {
    settingsRepository = mockk(relaxed = true)
    useCase = SetClientTagEnabledUseCaseImpl(settingsRepository)
  }

  @Test
  fun `invoke saves true to repository`() {
    useCase(true)

    verify { settingsRepository.setClientTagEnabled(true) }
  }

  @Test
  fun `invoke saves false to repository`() {
    useCase(false)

    verify { settingsRepository.setClientTagEnabled(false) }
  }
}
