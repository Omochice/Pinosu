package io.github.omochice.pinosu.feature.auth.domain.model.error

import io.github.omochice.pinosu.core.nip.nip55.Nip55Error
import org.junit.Assert.*
import org.junit.Test

class ErrorTypesTest {

  /** Test that LoginError.Nip55SignerNotInstalled can be created */
  @Test
  fun `create LoginError Nip55SignerNotInstalled`() {
    val error = LoginError.Nip55SignerNotInstalled

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.Nip55SignerNotInstalled)
  }

  /** Test that LoginError.UserRejected can be created */
  @Test
  fun `create LoginError UserRejected`() {
    val error = LoginError.UserRejected

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.UserRejected)
  }

  /** Test that LoginError.Timeout can be created */
  @Test
  fun `create LoginError Timeout`() {
    val error = LoginError.Timeout

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.Timeout)
  }

  /** Test that LoginError.NetworkError can be created */
  @Test
  fun `create LoginError NetworkError with message`() {
    val message = "Connection failed"
    val error = LoginError.NetworkError(message)

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.NetworkError)
    assertEquals(message, error.message)
  }

  /** Test that LoginError.UnknownError can be created */
  @Test
  fun `create LoginError UnknownError with throwable`() {
    val throwable = RuntimeException("Test exception")
    val error = LoginError.UnknownError(throwable)

    assertTrue(error is LoginError)
    assertTrue(error is LoginError.UnknownError)
    assertEquals(throwable, error.throwable)
  }

  /** Test LoginError when expression exhaustiveness */
  @Test
  fun `LoginError sealed class allows exhaustive when`() {
    val errors: List<LoginError> =
        listOf(
            LoginError.Nip55SignerNotInstalled,
            LoginError.UserRejected,
            LoginError.Timeout,
            LoginError.NetworkError("test"),
            LoginError.UnknownError(RuntimeException()))

    errors.forEach { error ->
      val result =
          when (error) {
            is LoginError.Nip55SignerNotInstalled -> "nip55_signer_not_installed"
            is LoginError.UserRejected -> "user_rejected"
            is LoginError.Timeout -> "timeout"
            is LoginError.NetworkError -> "network_error"
            is LoginError.UnknownError -> "unknown_error"
          }

      assertNotNull(result)
    }
  }

  /** Test that LogoutError.StorageError can be created */
  @Test
  fun `create LogoutError StorageError with message`() {
    val message = "Failed to clear storage"
    val error = LogoutError.StorageError(message)

    assertTrue(error is LogoutError)
    assertTrue(error is LogoutError.StorageError)
    assertEquals(message, error.message)
  }

  /** Test LogoutError when expression exhaustiveness */
  @Test
  fun `LogoutError sealed class allows exhaustive when`() {
    val error: LogoutError = LogoutError.StorageError("test")

    val result =
        when (error) {
          is LogoutError.StorageError -> "storage_error"
        }

    assertEquals("storage_error", result)
  }

  /** Test StorageError.WriteError can be created */
  @Test
  fun `create StorageError WriteError with message`() {
    val message = "Write failed"
    val error = StorageError.WriteError(message)

    assertTrue(error is StorageError)
    assertTrue(error is StorageError.WriteError)
    assertEquals(message, error.message)
  }

  /** Test StorageError.ReadError can be created */
  @Test
  fun `create StorageError ReadError with message`() {
    val message = "Read failed"
    val error = StorageError.ReadError(message)

    assertTrue(error is StorageError)
    assertTrue(error is StorageError.ReadError)
    assertEquals(message, error.message)
  }

  /** Test StorageError when expression exhaustiveness */
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

  /** Test Nip55Error.NotInstalled can be created */
  @Test
  fun `create Nip55Error NotInstalled`() {
    val error = Nip55Error.NotInstalled

    assertTrue(error is Nip55Error)
    assertTrue(error is Nip55Error.NotInstalled)
  }

  /** Test Nip55Error.UserRejected can be created */
  @Test
  fun `create Nip55Error UserRejected`() {
    val error = Nip55Error.UserRejected

    assertTrue(error is Nip55Error)
    assertTrue(error is Nip55Error.UserRejected)
  }

  /** Test Nip55Error.Timeout can be created */
  @Test
  fun `create Nip55Error Timeout`() {
    val error = Nip55Error.Timeout

    assertTrue(error is Nip55Error)
    assertTrue(error is Nip55Error.Timeout)
  }

  /** Test Nip55Error.InvalidResponse can be created */
  @Test
  fun `create Nip55Error InvalidResponse with message`() {
    val message = "Invalid response format"
    val error = Nip55Error.InvalidResponse(message)

    assertTrue(error is Nip55Error)
    assertTrue(error is Nip55Error.InvalidResponse)
    assertEquals(message, error.message)
  }

  /** Test Nip55Error.IntentResolutionError can be created */
  @Test
  fun `create Nip55Error IntentResolutionError with message`() {
    val message = "Intent resolution failed"
    val error = Nip55Error.IntentResolutionError(message)

    assertTrue(error is Nip55Error)
    assertTrue(error is Nip55Error.IntentResolutionError)
    assertEquals(message, error.message)
  }

  /** Test Nip55Error when expression exhaustiveness */
  @Test
  fun `Nip55Error sealed class allows exhaustive when`() {
    val errors: List<Nip55Error> =
        listOf(
            Nip55Error.NotInstalled,
            Nip55Error.UserRejected,
            Nip55Error.Timeout,
            Nip55Error.InvalidResponse("test"),
            Nip55Error.IntentResolutionError("test"))

    errors.forEach { error ->
      val result =
          when (error) {
            is Nip55Error.NotInstalled -> "not_installed"
            is Nip55Error.UserRejected -> "user_rejected"
            is Nip55Error.Timeout -> "timeout"
            is Nip55Error.InvalidResponse -> "invalid_response"
            is Nip55Error.IntentResolutionError -> "intent_resolution_error"
          }

      assertNotNull(result)
    }
  }
}
