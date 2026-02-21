package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.feature.auth.domain.model.error.LogoutError
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LogoutUseCase
 * - Logout success test
 * - Logout failure test
 * - Idempotency guarantee
 */
class LogoutUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var logoutUseCase: LogoutUseCase

  @Before
  fun setup() {
    authRepository = mockk(relaxed = true)
    logoutUseCase = Nip55LogoutUseCase(authRepository)
  }

  @Test
  fun `invoke on success should return success`() = runTest {
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val result = logoutUseCase()

    assertTrue("Should return success", result.isSuccess)
    coVerify { authRepository.logout() }
  }

  @Test
  fun `invoke on failure should return LogoutError`() = runTest {
    val error = LogoutError.StorageError("Failed to clear")
    coEvery { authRepository.logout() } returns Result.failure(error)

    val result = logoutUseCase()

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(
        "Exception should be LogoutError.StorageError", exception is LogoutError.StorageError)
    coVerify { authRepository.logout() }
  }

  @Test
  fun `invoke multiple calls should succeed (idempotency)`() = runTest {
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val result1 = logoutUseCase()
    val result2 = logoutUseCase()

    assertTrue("First call should succeed", result1.isSuccess)
    assertTrue("Second call should succeed", result2.isSuccess)
    coVerify(exactly = 2) { authRepository.logout() }
  }
}
