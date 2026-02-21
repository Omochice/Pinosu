package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Test class for GetLoginStateUseCase
 *
 * Test scenarios:
 * 1. Happy path: Retrieve logged-in user
 * 2. Happy path: Retrieve not-logged-in state
 * 3. Verify: Read-only operation
 */
class GetLoginStateUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase

  @Before
  fun setup() {
    authRepository = mockk()
    getLoginStateUseCase = Nip55GetLoginStateUseCase(authRepository)
  }

  @Test
  fun `invoke returns logged in user when user is logged in`() = runTest {
    val testPubkey = "npub1" + "a".repeat(59)
    val testUser = User(Pubkey.parse(testPubkey)!!)
    coEvery { authRepository.getLoginState() } returns testUser

    val result = getLoginStateUseCase()

    assertEquals(testUser, result)
    coVerify(exactly = 1) { authRepository.getLoginState() }
  }

  @Test
  fun `invoke returns null when user is not logged in`() = runTest {
    coEvery { authRepository.getLoginState() } returns null

    val result = getLoginStateUseCase()

    assertNull(result)
    coVerify(exactly = 1) { authRepository.getLoginState() }
  }

  @Test
  fun `invoke is read-only operation`() = runTest {
    val testPubkey = "npub1" + "b".repeat(59)
    val testUser = User(Pubkey.parse(testPubkey)!!)
    coEvery { authRepository.getLoginState() } returns testUser

    getLoginStateUseCase()
    getLoginStateUseCase()

    coVerify(exactly = 2) { authRepository.getLoginState() }
    coVerify(exactly = 0) { authRepository.saveLoginState(any(), any()) }
    coVerify(exactly = 0) { authRepository.logout() }
  }
}
