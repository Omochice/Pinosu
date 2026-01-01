package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.error.LogoutError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
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
    logoutUseCase = AmberLogoutUseCase(authRepository)
  }

  /** Test successful logout */
  @Test
  fun testInvoke_Success_ReturnsSuccess() = runTest {
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val result = logoutUseCase()

    assertTrue("Should return success", result.isSuccess)
    coVerify { authRepository.logout() }
  }

  /** Test logout failure */
  @Test
  fun testInvoke_Failure_ReturnsLogoutError() = runTest {
    val error = LogoutError.StorageError("Failed to clear")
    coEvery { authRepository.logout() } returns Result.failure(error)

    val result = logoutUseCase()

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(
        "Exception should be LogoutError.StorageError", exception is LogoutError.StorageError)
    coVerify { authRepository.logout() }
  }

  /** Idempotency test - multiple calls work correctly */
  @Test
  fun testInvoke_Idempotency_MultipleCallsSucceed() = runTest {
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val result1 = logoutUseCase()
    val result2 = logoutUseCase()

    assertTrue("First call should succeed", result1.isSuccess)
    assertTrue("Second call should succeed", result2.isSuccess)
    coVerify(exactly = 2) { authRepository.logout() }
  }
}
