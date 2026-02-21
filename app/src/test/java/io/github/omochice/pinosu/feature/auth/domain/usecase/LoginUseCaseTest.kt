package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoginUseCase
 * - NIP-55 signer installation verification test
 * - Error handling test
 */
class LoginUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var loginUseCase: LoginUseCase

  @Before
  fun setup() {
    authRepository = mockk(relaxed = true)
    loginUseCase = Nip55LoginUseCase(authRepository)
  }

  @Test
  fun `checkNip55SignerInstalled when not installed should return false`() {
    every { authRepository.checkNip55SignerInstalled() } returns false

    val result = loginUseCase.checkNip55SignerInstalled()

    assertFalse("Should return false when NIP-55 signer is not installed", result)
    verify { authRepository.checkNip55SignerInstalled() }
  }

  @Test
  fun `checkNip55SignerInstalled when installed should return true`() {
    every { authRepository.checkNip55SignerInstalled() } returns true

    val result = loginUseCase.checkNip55SignerInstalled()

    assertTrue("Should return true when NIP-55 signer is installed", result)
    verify { authRepository.checkNip55SignerInstalled() }
  }
}
