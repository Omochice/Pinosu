package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.error.LogoutError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runtest
import org.junit.Assert.*import org.junit.Before
import org.junit.test

/*** LogoutUseCaseUnit tests** Task 6.2: LogoutUseCaseImplementation of* - logoutsuccesstests* - logoutfailuretests* - of** Requirements: 2.4, 2.5*/class LogoutUseCasetest {

 private lateinit var authRepository: AuthRepository
 private lateinit var logoutUseCase: LogoutUseCase

 @Before
 fun setup() {
 authRepository = mockk(relaxed = true)
 logoutUseCase = AmberLogoutUseCase(authRepository)
 }

// ========== invoke() tests ==========
/*** logoutsuccesstests** Task 6.2: invoke()implementation Requirement 2.4: logoutfunctionality*/ @test
 fun testInvoke_Success_ReturnsSuccess() = runtest {
// Given: AuthRepositorysuccess coEvery { authRepository.logout() } returns Result.success(Unit)

// When: Call invoke() val result = logoutUseCase()

// Then: Success is returned assertTrue("Should return success", result.isSuccess)
 coVerify { authRepository.logout() }
 }

/*** logoutfailuretests** Task 6.2: invoke()implementation Requirement 2.5: error*/ @test
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

/*** tests - successfully** Task 6.2: of Requirement 2.5: logoutprocessingof*/ @test
 fun testInvoke_Idempotency_MultipleCallsSucceed() = runtest {
// Given: AuthRepositorysuccess coEvery { authRepository.logout() } returns Result.success(Unit)

// When: invoke()2call val result1 = logoutUseCase()
 val result2 = logoutUseCase()

// Then: success assertTrue("First call should succeed", result1.isSuccess)
 assertTrue("Second call should succeed", result2.isSuccess)
 coVerify(exactly = 2) { authRepository.logout() }
 }
}
