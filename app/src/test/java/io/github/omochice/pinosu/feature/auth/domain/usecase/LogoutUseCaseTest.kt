package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.core.logout.LogoutCleanup
import io.github.omochice.pinosu.feature.auth.domain.model.error.LogoutError
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for LogoutUseCase
 * - Logout success test
 * - Logout failure test
 * - Idempotency guarantee
 */
class LogoutUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var cleanup: LogoutCleanup
  private lateinit var logoutUseCase: LogoutUseCase

  @BeforeTest
  fun setup() {
    authRepository = mockk(relaxed = true)
    cleanup = mockk(relaxed = true)
    logoutUseCase = Nip55LogoutUseCase(authRepository, setOf(cleanup))
  }

  @Test
  fun `invoke on success should return success`() = runTest {
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val result = logoutUseCase()

    assertTrue(result.isSuccess, "Should return success")
    coVerify { authRepository.logout() }
  }

  @Test
  fun `invoke on failure should return LogoutError`() = runTest {
    val error = LogoutError.StorageError("Failed to clear")
    coEvery { authRepository.logout() } returns Result.failure(error)

    val result = logoutUseCase()

    assertTrue(result.isFailure, "Should return failure")
    val exception = result.exceptionOrNull()
    assertTrue(
        exception is LogoutError.StorageError, "Exception should be LogoutError.StorageError")
    coVerify { authRepository.logout() }
  }

  @Test
  fun `the data features keep for the user is deleted once the user is logged out`() = runTest {
    coEvery { authRepository.logout() } returns Result.success(Unit)

    logoutUseCase()

    coVerifyOrder {
      authRepository.logout()
      cleanup.cleanUp()
    }
  }

  @Test
  fun `the data features keep is left alone when the user could not be logged out`() = runTest {
    coEvery { authRepository.logout() } returns
        Result.failure(LogoutError.StorageError("Failed to clear"))

    logoutUseCase()

    coVerify(exactly = 0) { cleanup.cleanUp() }
  }

  @Test
  fun `invoke multiple calls should succeed (idempotency)`() = runTest {
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val result1 = logoutUseCase()
    val result2 = logoutUseCase()

    assertTrue(result1.isSuccess, "First call should succeed")
    assertTrue(result2.isSuccess, "Second call should succeed")
    coVerify(exactly = 2) { authRepository.logout() }
  }
}
