package io.github.omochice.pinosu.feature.auth.data.repository

import android.content.Intent
import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.nip.nip55.Nip55Response
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.model.error.LoginError
import io.github.omochice.pinosu.feature.auth.domain.model.error.LogoutError
import io.github.omochice.pinosu.feature.auth.domain.model.error.StorageError
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Nip55AuthRepository
 * - getLoginState(), saveLoginState(), logout() test
 * - loginWithNip55() test (NIP-55 signer not installed detection)
 * - Nip55Response processing and local save flow test
 * - NIP-55 signer success → local save success happy path test ✓
 * - NIP-55 signer failure error classification test ✓
 * - Logout handling test ✓
 * - Transaction consistency test ✓
 */
class Nip55AuthRepositoryTest {

  private lateinit var nip55SignerClient: Nip55SignerClient
  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var authRepository: AuthRepository

  @Before
  fun setup() {
    nip55SignerClient = mockk(relaxed = true)
    localAuthDataSource = mockk(relaxed = true)
    authRepository = Nip55AuthRepository(nip55SignerClient, localAuthDataSource)
  }

  @Test
  fun `getLoginState when user exists should return user`() = runTest {
    val expectedUser = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    coEvery { localAuthDataSource.getUser() } returns expectedUser

    val result = authRepository.getLoginState()

    assertEquals("Should return user from LocalAuthDataSource", expectedUser, result)
    coVerify { localAuthDataSource.getUser() }
  }

  @Test
  fun `getLoginState when no user should return null`() = runTest {
    coEvery { localAuthDataSource.getUser() } returns null

    val result = authRepository.getLoginState()

    assertNull("Should return null when no user is stored", result)
    coVerify { localAuthDataSource.getUser() }
  }

  @Test
  fun `saveLoginState on success should return success`() = runTest {
    val user = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    coEvery { localAuthDataSource.saveUser(user, any()) } returns Unit

    val result = authRepository.saveLoginState(user, LoginMode.Nip55Signer)

    assertTrue("Should return success", result.isSuccess)
    coVerify { localAuthDataSource.saveUser(user, LoginMode.Nip55Signer) }
  }

  @Test
  fun `saveLoginState on failure should return StorageError`() = runTest {
    val user = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    val storageError = StorageError.WriteError("Failed to save")
    coEvery { localAuthDataSource.saveUser(user, any()) } throws storageError

    val result = authRepository.saveLoginState(user, LoginMode.Nip55Signer)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be StorageError", exception is StorageError.WriteError)
    coVerify { localAuthDataSource.saveUser(user, LoginMode.Nip55Signer) }
  }

  @Test
  fun `saveLoginState should forward loginMode to localAuthDataSource`() = runTest {
    val user = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    coEvery { localAuthDataSource.saveUser(user, any()) } returns Unit

    authRepository.saveLoginState(user, LoginMode.ReadOnly)

    coVerify { localAuthDataSource.saveUser(user, LoginMode.ReadOnly) }
  }

  @Test
  fun `getLoginMode should delegate to localAuthDataSource`() = runTest {
    coEvery { localAuthDataSource.getLoginMode() } returns LoginMode.ReadOnly

    val result = authRepository.getLoginMode()

    assertEquals("Should return login mode from LocalAuthDataSource", LoginMode.ReadOnly, result)
    coVerify { localAuthDataSource.getLoginMode() }
  }

  @Test
  fun `saveRelayList should delegate to localAuthDataSource`() = runTest {
    val relays = listOf(RelayConfig("wss://relay.example.com"))
    coEvery { localAuthDataSource.saveRelayList(relays) } returns Unit

    authRepository.saveRelayList(relays)

    coVerify { localAuthDataSource.saveRelayList(relays) }
  }

  @Test
  fun `saveRelayList on failure should propagate StorageError`() = runTest {
    val relays = listOf(RelayConfig("wss://relay.example.com"))
    val storageError = StorageError.WriteError("Failed to save relay list")
    coEvery { localAuthDataSource.saveRelayList(relays) } throws storageError

    var thrown: Throwable? = null
    try {
      authRepository.saveRelayList(relays)
    } catch (e: StorageError) {
      thrown = e
    }

    assertTrue("Should throw StorageError", thrown is StorageError.WriteError)
  }

  @Test
  fun `logout on success should return success`() = runTest {
    coEvery { localAuthDataSource.clearLoginState() } returns Unit

    val result = authRepository.logout()

    assertTrue("Should return success", result.isSuccess)
    coVerify { localAuthDataSource.clearLoginState() }
  }

  @Test
  fun `logout on failure should return LogoutError`() = runTest {
    val storageError = StorageError.WriteError("Failed to clear")
    coEvery { localAuthDataSource.clearLoginState() } throws storageError

    val result = authRepository.logout()

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(
        "Exception should be LogoutError.StorageError", exception is LogoutError.StorageError)
    coVerify { localAuthDataSource.clearLoginState() }
  }

  @Test
  fun `processNip55Response on success should save user and return success`() = runTest {
    val pubkey = "npub1" + "a".repeat(59)
    val intent = Intent().apply { putExtra("result", pubkey) }
    val nip55Response = Nip55Response(pubkey, Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME)

    every { nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent) } returns
        Result.success(nip55Response)
    coEvery { localAuthDataSource.saveUser(any(), any()) } returns Unit

    val result = authRepository.processNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return success", result.isSuccess)
    val user = result.getOrNull()
    assertNotNull("User should not be null", user)
    assertEquals("Pubkey should match", pubkey, user?.pubkey?.npub)
    coVerify { localAuthDataSource.saveUser(any(), any()) }
  }

  @Test
  fun `processNip55Response when user rejected should return LoginError`() = runTest {
    val intent = Intent().apply { putExtra("rejected", true) }
    every { nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent) } returns
        Result.failure(io.github.omochice.pinosu.core.nip.nip55.Nip55Error.UserRejected)

    val result = authRepository.processNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.UserRejected", exception is LoginError.UserRejected)
  }

  @Test
  fun `processNip55Response when Nip55Signer not installed should return Nip55SignerNotInstalledError`() =
      runTest {
        val intent = Intent()
        every { nip55SignerClient.handleNip55Response(any(), any()) } returns
            Result.failure(io.github.omochice.pinosu.core.nip.nip55.Nip55Error.NotInstalled)

        val result = authRepository.processNip55Response(android.app.Activity.RESULT_OK, intent)

        assertTrue("Should return failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(
            "Exception should be LoginError.Nip55SignerNotInstalled",
            exception is LoginError.Nip55SignerNotInstalled)
      }

  @Test
  fun `processNip55Response on timeout should return TimeoutError`() = runTest {
    val intent = Intent()
    every { nip55SignerClient.handleNip55Response(any(), any()) } returns
        Result.failure(io.github.omochice.pinosu.core.nip.nip55.Nip55Error.Timeout)

    val result = authRepository.processNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.Timeout", exception is LoginError.Timeout)
  }

  @Test
  fun `processNip55Response on InvalidResponse should return NetworkError`() = runTest {
    val intent = Intent()
    every { nip55SignerClient.handleNip55Response(any(), any()) } returns
        Result.failure(
            io.github.omochice.pinosu.core.nip.nip55.Nip55Error.InvalidResponse("Invalid data"))

    val result = authRepository.processNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue("Exception should be LoginError.NetworkError", exception is LoginError.NetworkError)
  }

  @Test
  fun `processNip55Response when Nip55Signer success but local storage fails should return UnknownError`() =
      runTest {
        val pubkey = "npub1" + "a".repeat(59)
        val intent = Intent().apply { putExtra("result", pubkey) }
        val nip55Response = Nip55Response(pubkey, Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME)

        every {
          nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)
        } returns Result.success(nip55Response)
        coEvery { localAuthDataSource.saveUser(any(), any()) } throws
            StorageError.WriteError("Storage full")

        val result = authRepository.processNip55Response(android.app.Activity.RESULT_OK, intent)

        assertTrue("Should return failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(
            "Exception should be LoginError.UnknownError", exception is LoginError.UnknownError)

        val unknownError = exception as LoginError.UnknownError
        assertTrue(
            "Cause should be StorageError", unknownError.throwable is StorageError.WriteError)
      }

  @Test
  fun `checkNip55SignerInstalled when installed should return true`() {
    every { nip55SignerClient.checkNip55SignerInstalled() } returns true

    val result = authRepository.checkNip55SignerInstalled()

    assertTrue("Should return true when NIP-55 signer is installed", result)
  }

  @Test
  fun `checkNip55SignerInstalled when not installed should return false`() {
    every { nip55SignerClient.checkNip55SignerInstalled() } returns false

    val result = authRepository.checkNip55SignerInstalled()

    assertFalse("Should return false when NIP-55 signer is not installed", result)
  }
}
