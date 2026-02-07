package io.github.omochice.pinosu.feature.settings.data.repository

import io.github.omochice.pinosu.feature.settings.data.local.LocalSettingsDataSource
import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LocalSettingsRepositoryTest {

  private lateinit var dataSource: LocalSettingsDataSource
  private lateinit var repository: LocalSettingsRepository

  @Before
  fun setup() {
    dataSource = mockk(relaxed = true)
    repository = LocalSettingsRepository(dataSource)
  }

  @Test
  fun `getLocale delegates to data source`() {
    every { dataSource.getLocale() } returns AppLocale.English

    assertEquals(AppLocale.English, repository.getLocale())
  }

  @Test
  fun `setLocale delegates to data source`() {
    repository.setLocale(AppLocale.Japanese)

    verify { dataSource.setLocale(AppLocale.Japanese) }
  }

  @Test
  fun `setLocale System delegates to data source`() {
    repository.setLocale(AppLocale.System)

    verify { dataSource.setLocale(AppLocale.System) }
  }
}
