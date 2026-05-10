package io.github.omochice.pinosu.core.nip.nip55

import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for Nip55SignerClient
 * - checkNip55SignerInstalled() test
 * - Nip55Response, Nip55Error data class validation
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class Nip55SignerClientTest {

  private lateinit var context: Context

  private lateinit var packageManager: PackageManager

  private lateinit var nip55SignerClient: Nip55SignerClient

  @BeforeTest
  fun setup() {
    context = mockk(relaxed = true)
    packageManager = mockk(relaxed = true)
    every { context.packageManager } returns packageManager
    nip55SignerClient = Nip55SignerClient(context)
  }

  @Test
  fun `checkNip55SignerInstalled when installed should return true`() {
    every {
      packageManager.getPackageInfo(
          Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } returns android.content.pm.PackageInfo()

    val result = nip55SignerClient.checkNip55SignerInstalled()

    assertTrue(result, "Should return true when NIP-55 signer is installed")
  }

  @Test
  fun `checkNip55SignerInstalled when not installed should return false`() {
    every {
      packageManager.getPackageInfo(
          Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws PackageManager.NameNotFoundException()

    val result = nip55SignerClient.checkNip55SignerInstalled()

    assertFalse(result, "Should return false when NIP-55 signer is not installed")
  }

  @Test
  fun `checkNip55SignerInstalled on exception should return false`() {
    every {
      packageManager.getPackageInfo(
          Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws SecurityException("Unexpected error")

    val result = nip55SignerClient.checkNip55SignerInstalled()

    assertFalse(result, "Should return false on exception")
  }

  @Test
  fun `createPublicKeyIntent should have correct scheme`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    assertNotNull(intent.data, "Intent should have data URI")
    assertEquals(
        Nip55SignerClient.NOSTRSIGNER_SCHEME,
        intent.data?.scheme,
        "URI scheme should be nostrsigner")
  }

  @Test
  fun `createPublicKeyIntent should have correct package`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    assertEquals(
        Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME,
        intent.`package`,
        "Package should be NIP-55 signer package name")
  }

  @Test
  fun `createPublicKeyIntent should have correct type`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    assertEquals(
        Nip55SignerClient.TYPE_GET_PUBLIC_KEY,
        intent.getStringExtra("type"),
        "Type extra should be get_public_key")
  }

  @Test
  fun `createPublicKeyIntent should have correct flags`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    val expectedFlags =
        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP

    assertTrue(
        intent.flags and expectedFlags == expectedFlags,
        "Intent should have SINGLE_TOP and CLEAR_TOP flags")
  }

  @Test
  fun `createPublicKeyIntent should have correct action`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    assertEquals(
        android.content.Intent.ACTION_VIEW, intent.action, "Intent action should be ACTION_VIEW")
  }

  @Test
  fun `handleNip55Response on success should return Nip55Response`() {
    val pubkey = "npub1" + "a".repeat(59)
    val intent = android.content.Intent()
    intent.putExtra("result", pubkey)

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue(result.isSuccess, "Should return success")
    val response = result.getOrNull()
    assertNotNull(response, "Response should not be null")
    assertEquals(pubkey, response.pubkey, "Pubkey should match")
    assertEquals(
        Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME,
        response.packageName,
        "PackageName should be NIP-55 signer package")
  }

  @Test
  fun `handleNip55Response when user rejected should return error`() {
    val intent = android.content.Intent()
    intent.putExtra("rejected", true)

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue(result.isFailure, "Should return failure")
    val error = result.exceptionOrNull()
    assertTrue(
        error is Nip55Error.UserRejected || error.toString().contains("UserRejected"),
        "Error should be UserRejected")
  }

  @Test
  fun `handleNip55Response when result canceled should return UserRejected`() {
    val intent = android.content.Intent()

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_CANCELED, intent)

    assertTrue(result.isFailure, "Should return failure")
    val error = result.exceptionOrNull()
    assertTrue(
        error is Nip55Error.UserRejected || error.toString().contains("UserRejected"),
        "Error should be UserRejected")
  }

  @Test
  fun `handleNip55Response with null intent should return InvalidResponse`() {
    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, null)

    assertTrue(result.isFailure, "Should return failure")
    val error = result.exceptionOrNull()
    assertTrue(
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"),
        "Error should be InvalidResponse")
  }

  @Test
  fun `handleNip55Response with empty result should return InvalidResponse`() {
    val intent = android.content.Intent()
    intent.putExtra("result", "")

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue(result.isFailure, "Should return failure")
    val error = result.exceptionOrNull()
    assertTrue(
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"),
        "Error should be InvalidResponse")
  }

  @Test
  fun `handleNip55Response with invalid pubkey length should return InvalidResponse`() {
    val intent = android.content.Intent()
    intent.putExtra("result", "a".repeat(64))

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue(result.isFailure, "Should return failure")
    val error = result.exceptionOrNull()
    assertTrue(
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"),
        "Error should be InvalidResponse")
  }

  @Test
  fun `handleNip55Response with invalid pubkey format should return InvalidResponse`() {
    val intent = android.content.Intent()
    intent.putExtra("result", "nsec1" + "a".repeat(59))

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue(result.isFailure, "Should return failure")
    val error = result.exceptionOrNull()
    assertTrue(
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"),
        "Error should be InvalidResponse")
  }

  @Test
  fun `handleSignEventResponse on success should return SignedEventResponse`() {
    val signedJson = """{"id":"abc","sig":"xyz"}"""
    val intent = android.content.Intent()
    intent.putExtra("result", signedJson)

    val result = nip55SignerClient.handleSignEventResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue(result.isSuccess, "Should return success")
    assertEquals(signedJson, result.getOrNull()?.signedEventJson, "Signed event JSON should match")
  }

  @Test
  fun `handleSignEventResponse when canceled should return UserRejected`() {
    val intent = android.content.Intent()

    val result =
        nip55SignerClient.handleSignEventResponse(android.app.Activity.RESULT_CANCELED, intent)

    assertTrue(result.isFailure, "Should return failure")
    assertTrue(result.exceptionOrNull() is Nip55Error.UserRejected, "Error should be UserRejected")
  }

  @Test
  fun `handleSignEventResponse with null intent should return InvalidResponse`() {
    val result = nip55SignerClient.handleSignEventResponse(android.app.Activity.RESULT_OK, null)

    assertTrue(result.isFailure, "Should return failure")
    assertTrue(
        result.exceptionOrNull() is Nip55Error.InvalidResponse, "Error should be InvalidResponse")
  }

  @Test
  fun `handleSignEventResponse when rejected should return UserRejected`() {
    val intent = android.content.Intent()
    intent.putExtra("rejected", true)

    val result = nip55SignerClient.handleSignEventResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue(result.isFailure, "Should return failure")
    assertTrue(result.exceptionOrNull() is Nip55Error.UserRejected, "Error should be UserRejected")
  }

  @Test
  fun `handleSignEventResponse with empty result should return InvalidResponse`() {
    val intent = android.content.Intent()
    intent.putExtra("result", "")

    val result = nip55SignerClient.handleSignEventResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue(result.isFailure, "Should return failure")
    assertTrue(
        result.exceptionOrNull() is Nip55Error.InvalidResponse, "Error should be InvalidResponse")
  }

  @Test
  fun `maskPubkey with valid pubkey should return masked string`() {
    val pubkey = "npub1" + "abcdef0123456789".repeat(3) + "abcdef01234"

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("npub1abc...def01234", masked, "Should mask pubkey as first8...last8")
  }

  @Test
  fun `maskPubkey with different pubkey should return masked string`() {
    val pubkey = "npub1" + "1234567890abcdef".repeat(3) + "1234567890a"

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("npub1123...4567890a", masked, "Should mask pubkey as first8...last8")
  }

  @Test
  fun `maskPubkey with short pubkey should return original string`() {
    val pubkey = "abcdef0123456789"

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals(pubkey, masked, "Should return original string when pubkey is too short")
  }

  @Test
  fun `maskPubkey with empty string should return empty string`() {
    val pubkey = ""

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("", masked, "Should return empty string when input is empty")
  }

  @Test
  fun `maskPubkey result length should be correct`() {
    val pubkey = "npub1" + "a".repeat(59)

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals(19, masked.length, "Masked string should be 19 characters (8+3+8)")
  }
}
