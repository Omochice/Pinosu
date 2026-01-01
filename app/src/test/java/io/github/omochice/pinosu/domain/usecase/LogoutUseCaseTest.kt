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
 *
 * Requirements: 2.4, 2.5
 */
class LogoutUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var logoutUseCase: LogoutUseCase

  @Before
  fun setup() {
    authRepository = mockk(relaxed = true)
    logoutUseCase = AmberLogoutUseCase(authRepository)
  }

  // ========== invoke() Tests ==========

  /** ログアウト成功のテスト */
  @Test
  fun testInvoke_Success_ReturnsSuccess() = runTest {
    // Given: AuthRepositoryが成功を返す
    coEvery { authRepository.logout() } returns Result.success(Unit)

    // When: invoke()を呼び出す
    val result = logoutUseCase()

    // Then: Successが返される
    assertTrue("Should return success", result.isSuccess)
    coVerify { authRepository.logout() }
  }

  /** ログアウト失敗のテスト */
  @Test
  fun testInvoke_Failure_ReturnsLogoutError() = runTest {
    // Given: AuthRepositoryが失敗を返す
    val error = LogoutError.StorageError("Failed to clear")
    coEvery { authRepository.logout() } returns Result.failure(error)

    // When: invoke()を呼び出す
    val result = logoutUseCase()

    // Then: Failureが返される
    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(
        "Exception should be LogoutError.StorageError", exception is LogoutError.StorageError)
    coVerify { authRepository.logout() }
  }

  /** 冪等性のテスト - 複数回呼び出しても正常に動作 */
  @Test
  fun testInvoke_Idempotency_MultipleCallsSucceed() = runTest {
    // Given: AuthRepositoryが常に成功を返す
    coEvery { authRepository.logout() } returns Result.success(Unit)

    // When: invoke()を2回呼び出す
    val result1 = logoutUseCase()
    val result2 = logoutUseCase()

    // Then: 両方とも成功する
    assertTrue("First call should succeed", result1.isSuccess)
    assertTrue("Second call should succeed", result2.isSuccess)
    coVerify(exactly = 2) { authRepository.logout() }
  }
}
