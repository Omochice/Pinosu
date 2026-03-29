package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetClientTagEnabledUseCaseTest {

  private lateinit var clientTagRepository: ClientTagRepository
  private lateinit var useCase: SetClientTagEnabledUseCase

  @Before
  fun setup() {
    clientTagRepository = mockk(relaxed = true)
    useCase = SetClientTagEnabledUseCaseImpl(clientTagRepository)
  }

  @Test
  fun `invoke saves true to repository`() {
    useCase(true)

    verify { clientTagRepository.setClientTagEnabled(true) }
  }

  @Test
  fun `invoke saves false to repository`() {
    useCase(false)

    verify { clientTagRepository.setClientTagEnabled(false) }
  }
}
