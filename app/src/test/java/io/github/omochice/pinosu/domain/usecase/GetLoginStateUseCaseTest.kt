package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runtest
import org.junit.Assert.*
import org.junit.Before
import org.junit.test

/**
 * test class for GetLoginStateUseCase
 *
 * Task 6.3: Implementation of GetLoginStateUseCase Requirements: 2.2, 2.3
 *
 * test scenarios:
 * 1. Normal case: Get logged in user
 * 2. Normal case: Get not logged in state
 * 3. Verification: Read-only operation
 */
class GetLoginStateUseCasetest {

  private lateinit var authRepository: AuthRepository
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase

  @Before
  fun setup() {
    authRepository = mockk()
    getLoginStateUseCase = AmberGetLoginStateUseCase(authRepository)
  }

  @test
  fun `invoke returns logged in user when user is logged in`() = runtest {
    // Given: Logged in state
    val testPubkey = "npub1" + "a".repeat(59)
    val testUser = User(testPubkey)
    coEvery { authRepository.getLoginState() } returns testUser

    // When: Get login state
    val result = getLoginStateUseCase()

    // Then: User is returned
    assertEquals(testUser, result)
    coVerify(exactly = 1) { authRepository.getLoginState() }
  }

  @test
  fun `invoke returns null when user is not logged in`() = runtest {
    // Given: Not logged in state
    coEvery { authRepository.getLoginState() } returns null

    // When: Get login state
    val result = getLoginStateUseCase()

    // Then: null is returned
    assertNull(result)
    coVerify(exactly = 1) { authRepository.getLoginState() }
  }

  @test
  fun `invoke is read-only operation`() = runtest {
    // Given: Logged in state
    val testPubkey = "npub1" + "b".repeat(59)
    val testUser = User(testPubkey)
    coEvery { authRepository.getLoginState() } returns testUser

    // When: Call multiple times
    getLoginStateUseCase()
    getLoginStateUseCase()

    // Then: Only calls to AuthRepository, no modification operations
    coVerify(exactly = 2) { authRepository.getLoginState() }
    coVerify(exactly = 0) { authRepository.saveLoginState(any()) }
    coVerify(exactly = 0) { authRepository.logout() }
  }
}
