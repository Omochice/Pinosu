package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.error.LogoutError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runtest
import org.junit.Assert.*import org.junit.Before
import org.junit.test


 private lateinit var authRepository: AuthRepository
 private lateinit var logoutUseCase: LogoutUseCase

 @Before
 fun setup() {
 authRepository = mockk(relaxed = true)
 logoutUseCase = AmberLogoutUseCase(authRepository)
 }

// ========== invoke() tests ==========
 fun testInvoke_Success_ReturnsSuccess() = runtest {
// Given: AuthRepositorysuccess coEvery { authRepository.logout() } returns Result.success(Unit)

// When: Call invoke() val result = logoutUseCase()

// Then: Success is returned assertTrue("Should return success", result.isSuccess)
 coVerify { authRepository.logout() }
 }

 fun testInvoke_Failure_ReturnsLogoutError() = runtest {
// Given: AuthRepositoryfailure val error = LogoutError.StorageError("Failed to clear")
 coEvery { authRepository.logout() } returns Result.failure(error)

// When: Call invoke() val result = logoutUseCase()

// Then: Failure is returned assertTrue("Should return failure", result.isFailure)
 val exception = result.exceptionOrNull()
 assertTrue(
 "Exception should be LogoutError.StorageError", exception is LogoutError.StorageError)
 coVerify { authRepository.logout() }
 }

 fun testInvoke_Idempotency_MultipleCallsSucceed() = runtest {
// Given: AuthRepositorysuccess coEvery { authRepository.logout() } returns Result.success(Unit)

// When: invoke()2call val result1 = logoutUseCase()
 val result2 = logoutUseCase()

// Then: success assertTrue("First call should succeed", result1.isSuccess)
 assertTrue("Second call should succeed", result2.isSuccess)
 coVerify(exactly = 2) { authRepository.logout() }
 }
}
