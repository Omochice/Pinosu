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
 * LogoutUseCaseの単体テスト
 * - ログアウト成功のテスト
 * - ログアウト失敗のテスト
 * - 冪等性の保証
 */
class LogoutUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var logoutUseCase: LogoutUseCase

  @Before
  fun setup() {
    authRepository = mockk(relaxed = true)
    logoutUseCase = AmberLogoutUseCase(authRepository)
  }

  /** ログアウト成功のテスト */
  @Test
  fun testInvoke_Success_ReturnsSuccess() = runTest {
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val result = logoutUseCase()

    assertTrue("Should return success", result.isSuccess)
    coVerify { authRepository.logout() }
  }

  /** ログアウト失敗のテスト */
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

  /** 冪等性のテスト - 複数回呼び出しても正常に動作 */
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
