package io.github.omochice.pinosu.data.nip55

import android.content.Context
import android.content.pm.PackageManager
import io.github.omochice.pinosu.core.nip.nip55.Nip55Error
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
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

  @Before
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

    assertTrue("Should return true when NIP-55 signer is installed", result)
  }

  @Test
  fun `checkNip55SignerInstalled when not installed should return false`() {
    every {
      packageManager.getPackageInfo(
          Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws PackageManager.NameNotFoundException()

    val result = nip55SignerClient.checkNip55SignerInstalled()

    assertFalse("Should return false when NIP-55 signer is not installed", result)
  }

  @Test
  fun `checkNip55SignerInstalled on exception should return false`() {
    every {
      packageManager.getPackageInfo(
          Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws RuntimeException("Unexpected error")

    val result = nip55SignerClient.checkNip55SignerInstalled()

    assertFalse("Should return false on exception", result)
  }

  @Test
  fun `createPublicKeyIntent should have correct scheme`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    assertNotNull("Intent should have data URI", intent.data)
    assertEquals(
        "URI scheme should be nostrsigner",
        Nip55SignerClient.NOSTRSIGNER_SCHEME,
        intent.data?.scheme)
  }

  @Test
  fun `createPublicKeyIntent should have correct package`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    assertEquals(
        "Package should be NIP-55 signer package name",
        Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME,
        intent.`package`)
  }

  @Test
  fun `createPublicKeyIntent should have correct type`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    assertEquals(
        "Type extra should be get_public_key",
        Nip55SignerClient.TYPE_GET_PUBLIC_KEY,
        intent.getStringExtra("type"))
  }

  @Test
  fun `createPublicKeyIntent should have correct flags`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    val expectedFlags =
        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP

    assertTrue(
        "Intent should have SINGLE_TOP and CLEAR_TOP flags",
        (intent.flags and expectedFlags) == expectedFlags)
  }

  @Test
  fun `createPublicKeyIntent should have correct action`() {
    val intent = nip55SignerClient.createPublicKeyIntent()

    assertEquals(
        "Intent action should be ACTION_VIEW", android.content.Intent.ACTION_VIEW, intent.action)
  }

  @Test
  fun `handleNip55Response on success should return Nip55Response`() {
    val pubkey = "npub1" + "a".repeat(59)
    val intent = android.content.Intent()
    intent.putExtra("result", pubkey)

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return success", result.isSuccess)
    val response = result.getOrNull()
    assertNotNull("Response should not be null", response)
    assertEquals("Pubkey should match", pubkey, response?.pubkey)
    assertEquals(
        "PackageName should be NIP-55 signer package",
        Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME,
        response?.packageName)
  }

  @Test
  fun `handleNip55Response when user rejected should return error`() {
    val intent = android.content.Intent()
    intent.putExtra("rejected", true)

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is Nip55Error.UserRejected || error.toString().contains("UserRejected"))
  }

  @Test
  fun `handleNip55Response when result canceled should return UserRejected`() {
    val intent = android.content.Intent()

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_CANCELED, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is Nip55Error.UserRejected || error.toString().contains("UserRejected"))
  }

  @Test
  fun `handleNip55Response with null intent should return InvalidResponse`() {
    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, null)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  @Test
  fun `handleNip55Response with empty result should return InvalidResponse`() {
    val intent = android.content.Intent()
    intent.putExtra("result", "")

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  @Test
  fun `handleNip55Response with invalid pubkey length should return InvalidResponse`() {
    val intent = android.content.Intent()
    intent.putExtra("result", "a".repeat(64))

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  @Test
  fun `handleNip55Response with invalid pubkey format should return InvalidResponse`() {
    val intent = android.content.Intent()
    intent.putExtra("result", "nsec1" + "a".repeat(59))

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  @Test
  fun `maskPubkey with valid pubkey should return masked string`() {
    val pubkey = "npub1" + "abcdef0123456789".repeat(3) + "abcdef01234"

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Should mask pubkey as first8...last8", "npub1abc...def01234", masked)
  }

  @Test
  fun `maskPubkey with different pubkey should return masked string`() {
    val pubkey = "npub1" + "1234567890abcdef".repeat(3) + "1234567890a"

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Should mask pubkey as first8...last8", "npub1123...4567890a", masked)
  }

  @Test
  fun `maskPubkey with short pubkey should return original string`() {
    val pubkey = "abcdef0123456789"

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Should return original string when pubkey is too short", pubkey, masked)
  }

  @Test
  fun `maskPubkey with empty string should return empty string`() {
    val pubkey = ""

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Should return empty string when input is empty", "", masked)
  }

  @Test
  fun `maskPubkey result length should be correct`() {
    val pubkey = "npub1" + "a".repeat(59)

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Masked string should be 19 characters (8+3+8)", 19, masked.length)
  }
}
