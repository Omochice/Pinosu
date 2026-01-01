package io.github.omochice.pinosu.domain.model.error

import org.junit.Assert.*import org.junit.test


// ========== LoginError tests ==========
/** LoginError.AmberNotInstalledtest that User can be created */ @test
 fun `create LoginError AmberNotInstalled`() {
 val error = LoginError.AmberNotInstalled

 assertTrue(error is LoginError)
 assertTrue(error is LoginError.AmberNotInstalled)
 }

/** LoginError.UserRejectedtest that User can be created */ @test
 fun `create LoginError UserRejected`() {
 val error = LoginError.UserRejected

 assertTrue(error is LoginError)
 assertTrue(error is LoginError.UserRejected)
 }

/** LoginError.Timeouttest that User can be created */ @test
 fun `create LoginError Timeout`() {
 val error = LoginError.Timeout

 assertTrue(error is LoginError)
 assertTrue(error is LoginError.Timeout)
 }

/** LoginError.NetworkErrortest that User can be created */ @test
 fun `create LoginError NetworkError with message`() {
 val message = "Connection failed"
 val error = LoginError.NetworkError(message)

 assertTrue(error is LoginError)
 assertTrue(error is LoginError.NetworkError)
 assertEquals(message, error.message)
 }

/** LoginError.UnknownErrortest that User can be created */ @test
 fun `create LoginError UnknownError with throwable`() {
 val throwable = RuntimeException("test exception")
 val error = LoginError.UnknownError(throwable)

 assertTrue(error is LoginError)
 assertTrue(error is LoginError.UnknownError)
 assertEquals(throwable, error.throwable)
 }

/** LoginErrorofwhentest */ @test
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

// ========== LogoutError tests ==========
/** LogoutError.StorageErrortest that User can be created */ @test
 fun `create LogoutError StorageError with message`() {
 val message = "Failed to clear storage"
 val error = LogoutError.StorageError(message)

 assertTrue(error is LogoutError)
 assertTrue(error is LogoutError.StorageError)
 assertEquals(message, error.message)
 }

/** LogoutErrorofwhentest */ @test
 fun `LogoutError sealed class allows exhaustive when`() {
 val error: LogoutError = LogoutError.StorageError("test")

 val result =
 when (error) {
 is LogoutError.StorageError -> "storage_error"
 }

 assertEquals("storage_error", result)
 }

// ========== StorageError tests ==========
/** StorageError.WriteErrortest that User can be created */ @test
 fun `create StorageError WriteError with message`() {
 val message = "Write failed"
 val error = StorageError.WriteError(message)

 assertTrue(error is StorageError)
 assertTrue(error is StorageError.WriteError)
 assertEquals(message, error.message)
 }

/** StorageError.ReadErrortest that User can be created */ @test
 fun `create StorageError ReadError with message`() {
 val message = "Read failed"
 val error = StorageError.ReadError(message)

 assertTrue(error is StorageError)
 assertTrue(error is StorageError.ReadError)
 assertEquals(message, error.message)
 }

/** StorageErrorofwhentest */ @test
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

// ========== AmberError tests ==========
/** AmberError.NotInstalledtest that User can be created */ @test
 fun `create AmberError NotInstalled`() {
 val error = AmberError.NotInstalled

 assertTrue(error is AmberError)
 assertTrue(error is AmberError.NotInstalled)
 }

/** AmberError.UserRejectedtest that User can be created */ @test
 fun `create AmberError UserRejected`() {
 val error = AmberError.UserRejected

 assertTrue(error is AmberError)
 assertTrue(error is AmberError.UserRejected)
 }

/** AmberError.Timeouttest that User can be created */ @test
 fun `create AmberError Timeout`() {
 val error = AmberError.Timeout

 assertTrue(error is AmberError)
 assertTrue(error is AmberError.Timeout)
 }

/** AmberError.InvalidResponsetest that User can be created */ @test
 fun `create AmberError InvalidResponse with message`() {
 val message = "Invalid response format"
 val error = AmberError.InvalidResponse(message)

 assertTrue(error is AmberError)
 assertTrue(error is AmberError.InvalidResponse)
 assertEquals(message, error.message)
 }

/** AmberError.IntentResolutionErrortest that User can be created */ @test
 fun `create AmberError IntentResolutionError with message`() {
 val message = "Intent resolution failed"
 val error = AmberError.IntentResolutionError(message)

 assertTrue(error is AmberError)
 assertTrue(error is AmberError.IntentResolutionError)
 assertEquals(message, error.message)
 }

/** AmberErrorofwhentest */ @test
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
