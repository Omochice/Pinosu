package io.github.omochice.pinosu.domain.model.error

import org.junit.Assert.*
import org.junit.Test

class ErrorTypesTest {

  // ========== LoginError Tests ==========

  /** LoginError.AmberNotInstalledを作成できることをテスト */
  @Test
  fun `create LoginError AmberNotInstalled`() {
    val error = LoginError.AmberNotInstalled

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.AmberNotInstalled)
  }

  /** LoginError.UserRejectedを作成できることをテスト */
  @Test
  fun `create LoginError UserRejected`() {
    val error = LoginError.UserRejected

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.UserRejected)
  }

  /** LoginError.Timeoutを作成できることをテスト */
  @Test
  fun `create LoginError Timeout`() {
    val error = LoginError.Timeout

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.Timeout)
  }

  /** LoginError.NetworkErrorを作成できることをテスト */
  @Test
  fun `create LoginError NetworkError with message`() {
    val message = "Connection failed"
    val error = LoginError.NetworkError(message)

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.NetworkError)
    assertEquals(message, error.message)
  }

  /** LoginError.UnknownErrorを作成できることをテスト */
  @Test
  fun `create LoginError UnknownError with throwable`() {
    val throwable = RuntimeException("Test exception")
    val error = LoginError.UnknownError(throwable)

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.UnknownError)
    assertEquals(throwable, error.throwable)
  }

  /** LoginErrorのwhen式網羅性をテスト */
  @Test
  fun `LoginError sealed class allows exhaustive when`() {
    val errors: List<LoginError> =
        listOf(
            LoginError.AmberNotInstalled,
            LoginError.UserRejected,
            LoginError.Timeout,
            LoginError.NetworkError("test"),
            LoginError.UnknownError(RuntimeException()))

    errors.forEach { error ->
      val result =
          when (error) {
            is LoginError.AmberNotInstalled -> "amber_not_installed"
            is LoginError.UserRejected -> "user_rejected"
            is LoginError.Timeout -> "timeout"
            is LoginError.NetworkError -> "network_error"
            is LoginError.UnknownError -> "unknown_error"
          }

      assertNotNull(result)
    }
  }

  // ========== LogoutError Tests ==========

  /** LogoutError.StorageErrorを作成できることをテスト */
  @Test
  fun `create LogoutError StorageError with message`() {
    val message = "Failed to clear storage"
    val error = LogoutError.StorageError(message)

    assertTrue(error is LogoutError)
    assertTrue(error is LogoutError.StorageError)
    assertEquals(message, error.message)
  }

  /** LogoutErrorのwhen式網羅性をテスト */
  @Test
  fun `LogoutError sealed class allows exhaustive when`() {
    val error: LogoutError = LogoutError.StorageError("test")

    val result =
        when (error) {
          is LogoutError.StorageError -> "storage_error"
        }

    assertEquals("storage_error", result)
  }

  // ========== StorageError Tests ==========

  /** StorageError.WriteErrorを作成できることをテスト */
  @Test
  fun `create StorageError WriteError with message`() {
    val message = "Write failed"
    val error = StorageError.WriteError(message)

    assertTrue(error is StorageError)
    assertTrue(error is StorageError.WriteError)
    assertEquals(message, error.message)
  }

  /** StorageError.ReadErrorを作成できることをテスト */
  @Test
  fun `create StorageError ReadError with message`() {
    val message = "Read failed"
    val error = StorageError.ReadError(message)

    assertTrue(error is StorageError)
    assertTrue(error is StorageError.ReadError)
    assertEquals(message, error.message)
  }

  /** StorageErrorのwhen式網羅性をテスト */
  @Test
  fun `StorageError sealed class allows exhaustive when`() {
    val errors: List<StorageError> =
        listOf(StorageError.WriteError("write"), StorageError.ReadError("read"))

    errors.forEach { error ->
      val result =
          when (error) {
            is StorageError.WriteError -> "write_error"
            is StorageError.ReadError -> "read_error"
          }

      assertNotNull(result)
    }
  }

  // ========== AmberError Tests ==========

  /** AmberError.NotInstalledを作成できることをテスト */
  @Test
  fun `create AmberError NotInstalled`() {
    val error = AmberError.NotInstalled

    assertTrue(error is AmberError)
    assertTrue(error is AmberError.NotInstalled)
  }

  /** AmberError.UserRejectedを作成できることをテスト */
  @Test
  fun `create AmberError UserRejected`() {
    val error = AmberError.UserRejected

    assertTrue(error is AmberError)
    assertTrue(error is AmberError.UserRejected)
  }

  /** AmberError.Timeoutを作成できることをテスト */
  @Test
  fun `create AmberError Timeout`() {
    val error = AmberError.Timeout

    assertTrue(error is AmberError)
    assertTrue(error is AmberError.Timeout)
  }

  /** AmberError.InvalidResponseを作成できることをテスト */
  @Test
  fun `create AmberError InvalidResponse with message`() {
    val message = "Invalid response format"
    val error = AmberError.InvalidResponse(message)

    assertTrue(error is AmberError)
    assertTrue(error is AmberError.InvalidResponse)
    assertEquals(message, error.message)
  }

  /** AmberError.IntentResolutionErrorを作成できることをテスト */
  @Test
  fun `create AmberError IntentResolutionError with message`() {
    val message = "Intent resolution failed"
    val error = AmberError.IntentResolutionError(message)

    assertTrue(error is AmberError)
    assertTrue(error is AmberError.IntentResolutionError)
    assertEquals(message, error.message)
  }

  /** AmberErrorのwhen式網羅性をテスト */
  @Test
  fun `AmberError sealed class allows exhaustive when`() {
    val errors: List<AmberError> =
        listOf(
            AmberError.NotInstalled,
            AmberError.UserRejected,
            AmberError.Timeout,
            AmberError.InvalidResponse("test"),
            AmberError.IntentResolutionError("test"))

    errors.forEach { error ->
      val result =
          when (error) {
            is AmberError.NotInstalled -> "not_installed"
            is AmberError.UserRejected -> "user_rejected"
            is AmberError.Timeout -> "timeout"
            is AmberError.InvalidResponse -> "invalid_response"
            is AmberError.IntentResolutionError -> "intent_resolution_error"
          }

      assertNotNull(result)
    }
  }
}
