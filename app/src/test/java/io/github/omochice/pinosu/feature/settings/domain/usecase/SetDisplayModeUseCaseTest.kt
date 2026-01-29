package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.data.repository.SettingsRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetDisplayModeUseCaseTest {

  private lateinit var settingsRepository: SettingsRepository
  private lateinit var useCase: SetDisplayModeUseCase

  @Before
  fun setup() {
    settingsRepository = mockk(relaxed = true)
    useCase = SetDisplayModeUseCaseImpl(settingsRepository)
  }

  @Test
  fun `invoke saves List mode to repository`() {
    useCase(BookmarkDisplayMode.List)

    verify { settingsRepository.setDisplayMode(BookmarkDisplayMode.List) }
  }

  @Test
  fun `invoke saves Grid mode to repository`() {
    useCase(BookmarkDisplayMode.Grid)

    verify { settingsRepository.setDisplayMode(BookmarkDisplayMode.Grid) }
  }
}
