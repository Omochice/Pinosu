package io.github.omochice.pinosu.feature.auth.domain.usecase

import io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
import io.github.omochice.pinosu.feature.auth.domain.model.error.LoginError
import io.github.omochice.pinosu.feature.auth.domain.model.error.StorageError
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ReadOnlyLoginUseCase
 *
 * Verifies npub parsing, user persistence with ReadOnly login mode, and error handling for invalid
 * npub and storage failures.
 */
class ReadOnlyLoginUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var useCase: ReadOnlyLoginUseCase

  @Before
  fun setup() {
    authRepository = mockk(relaxed = true)
    useCase = ReadOnlyLoginUseCaseImpl(authRepository)
  }

  @Test
  fun `invoke with valid npub should save user with ReadOnly mode and return success`() = runTest {
    val npub = TEST_VALID_NPUB
    coEvery { authRepository.saveLoginState(any(), any()) } returns Result.success(Unit)

    val result = useCase(npub)

    assertTrue("Should return success", result.isSuccess)
    val user = result.getOrNull()!!
    assertEquals("Pubkey should match", npub, user.pubkey.npub)
    coVerify { authRepository.saveLoginState(any(), eq(LoginMode.ReadOnly)) }
  }

  @Test
  fun `invoke with invalid npub should return failure`() = runTest {
    val result = useCase("invalid_npub")

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue("Error should be InvalidPubkey", error is LoginError.InvalidPubkey)
  }

  @Test
  fun `invoke when storage fails should return UnknownError`() = runTest {
    val npub = TEST_VALID_NPUB
    coEvery { authRepository.saveLoginState(any(), any()) } returns
        Result.failure(StorageError.WriteError("Storage full"))

    val result = useCase(npub)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue("Error should be UnknownError", error is LoginError.UnknownError)
  }

  companion object {
    /** fiatjaf's npub - a real bech32-encoded public key that passes checksum validation */
    private const val TEST_VALID_NPUB =
        "npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m"
  }
}
