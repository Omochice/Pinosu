package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.SettingsRepository
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetDisplayModeUseCaseTest {

  private lateinit var settingsRepository: SettingsRepository
  private lateinit var useCase: GetDisplayModeUseCase

  @Before
  fun setup() {
    settingsRepository = mockk()
    useCase = GetDisplayModeUseCaseImpl(settingsRepository)
  }

  @Test
  fun `invoke returns List when repository returns List`() {
    every { settingsRepository.getDisplayMode() } returns BookmarkDisplayMode.List

    val result = useCase()

    assertEquals(BookmarkDisplayMode.List, result)
    verify { settingsRepository.getDisplayMode() }
  }

  @Test
  fun `invoke returns Grid when repository returns Grid`() {
    every { settingsRepository.getDisplayMode() } returns BookmarkDisplayMode.Grid

    val result = useCase()

    assertEquals(BookmarkDisplayMode.Grid, result)
    verify { settingsRepository.getDisplayMode() }
  }
}
