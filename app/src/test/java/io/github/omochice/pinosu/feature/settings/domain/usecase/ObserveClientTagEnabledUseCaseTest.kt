package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveClientTagEnabledUseCaseTest {

  @Test
  fun `invoke returns clientTagEnabledFlow from repository`() {
    val clientTagRepository: ClientTagRepository = mockk(relaxed = true)
    val flow = MutableStateFlow(true)
    every { clientTagRepository.clientTagEnabledFlow } returns flow

    val useCase = ObserveClientTagEnabledUseCaseImpl(clientTagRepository)

    assertEquals(true, useCase().value)
  }

  @Test
  fun `invoke returns false when repository emits false`() {
    val clientTagRepository: ClientTagRepository = mockk(relaxed = true)
    val flow = MutableStateFlow(false)
    every { clientTagRepository.clientTagEnabledFlow } returns flow

    val useCase = ObserveClientTagEnabledUseCaseImpl(clientTagRepository)

    assertEquals(false, useCase().value)
  }
}
