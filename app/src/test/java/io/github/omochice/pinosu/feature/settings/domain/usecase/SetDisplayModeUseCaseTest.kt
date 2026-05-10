package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class SetDisplayModeUseCaseTest {

  private lateinit var settingsRepository: SettingsRepository
  private lateinit var useCase: SetDisplayModeUseCase

  @BeforeTest
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
