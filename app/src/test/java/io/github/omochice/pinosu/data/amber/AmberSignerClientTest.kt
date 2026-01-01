package io.github.omochice.pinosu.data.amber

import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for AmberSignerClient
 * - checkAmberInstalled() test
 * - AmberResponse, AmberError data class validation
 */
@RunWith(RobolectricTestRunner::class)
class AmberSignerClientTest {

  private lateinit var context: Context

  private lateinit var packageManager: PackageManager

  private lateinit var amberSignerClient: AmberSignerClient

  @Before
  fun setup() {
    context = mockk(relaxed = true)
    packageManager = mockk(relaxed = true)
    every { context.packageManager } returns packageManager
    amberSignerClient = AmberSignerClient(context)
  }

  /** Test that returns true when Amber is installed */
  @Test
  fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {

    every {
      packageManager.getPackageInfo(
          AmberSignerClient.AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } returns android.content.pm.PackageInfo()

    val result = amberSignerClient.checkAmberInstalled()

    assertTrue("Should return true when Amber is installed", result)
  }

  /** Test that returns false when Amber is not installed */
  @Test
  fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {

    every {
      packageManager.getPackageInfo(
          AmberSignerClient.AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws PackageManager.NameNotFoundException()

    val result = amberSignerClient.checkAmberInstalled()

    assertFalse("Should return false when Amber is not installed", result)
  }

  /** Test that returns false when PackageManager throws exception */
  @Test
  fun testCheckAmberInstalled_OnException_ReturnsFalse() {

    every {
      packageManager.getPackageInfo(
          AmberSignerClient.AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws RuntimeException("Unexpected error")

    val result = amberSignerClient.checkAmberInstalled()

    assertFalse("Should return false on exception", result)
  }

  /** Test that AmberResponse data class is constructed correctly */
  @Test
  fun testAmberResponse_Construction() {

    val pubkey = "npub1" + "a".repeat(59)
    val packageName = "com.greenart7c3.nostrsigner"

    val response = AmberResponse(pubkey, packageName)

    assertEquals("Pubkey should match", pubkey, response.pubkey)
    assertEquals("PackageName should match", packageName, response.packageName)
  }

  /** Test that AmberResponse equality is determined correctly */
  @Test
  fun testAmberResponse_Equality() {

    val response1 = AmberResponse("npub1" + "abc".repeat(19) + "ab", "com.test.app")
    val response2 = AmberResponse("npub1" + "abc".repeat(19) + "ab", "com.test.app")

    assertEquals("Responses with same values should be equal", response1, response2)
  }

  /** Test AmberError.NotInstalled is constructed correctly */
  @Test
  fun testAmberError_NotInstalled() {

    val error: AmberError = AmberError.NotInstalled

    assertTrue("Should be NotInstalled type", error is AmberError.NotInstalled)
  }

  /** Test AmberError.UserRejected is constructed correctly */
  @Test
  fun testAmberError_UserRejected() {

    val error: AmberError = AmberError.UserRejected

    assertTrue("Should be UserRejected type", error is AmberError.UserRejected)
  }

  /** Test AmberError.Timeout is constructed correctly */
  @Test
  fun testAmberError_Timeout() {

    val error: AmberError = AmberError.Timeout

    assertTrue("Should be Timeout type", error is AmberError.Timeout)
  }

  /** Test AmberError.InvalidResponse is constructed correctly */
  @Test
  fun testAmberError_InvalidResponse() {

    val message = "Invalid response format"

    val error: AmberError = AmberError.InvalidResponse(message)

    assertTrue("Should be InvalidResponse type", error is AmberError.InvalidResponse)
    assertEquals("Message should match", message, (error as AmberError.InvalidResponse).message)
  }

  /** Test AmberError.IntentResolutionError is constructed correctly */
  @Test
  fun testAmberError_IntentResolutionError() {

    val message = "Cannot resolve intent"

    val error: AmberError = AmberError.IntentResolutionError(message)

    assertTrue("Should be IntentResolutionError type", error is AmberError.IntentResolutionError)
    assertEquals(
        "Message should match", message, (error as AmberError.IntentResolutionError).message)
  }

  /** Test createPublicKeyIntent() creates Intent with correct scheme */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectScheme() {

    val intent = amberSignerClient.createPublicKeyIntent()

    assertNotNull("Intent should have data URI", intent.data)
    assertEquals(
        "URI scheme should be nostrsigner",
        AmberSignerClient.NOSTRSIGNER_SCHEME,
        intent.data?.scheme)
  }

  /** Test createPublicKeyIntent() sets correct package name */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectPackage() {

    val intent = amberSignerClient.createPublicKeyIntent()

    assertEquals(
        "Package should be Amber package name",
        AmberSignerClient.AMBER_PACKAGE_NAME,
        intent.`package`)
  }

  /** Test createPublicKeyIntent() sets get_public_key type */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectType() {

    val intent = amberSignerClient.createPublicKeyIntent()

    assertEquals(
        "Type extra should be get_public_key",
        AmberSignerClient.TYPE_GET_PUBLIC_KEY,
        intent.getStringExtra("type"))
  }

  /** Test createPublicKeyIntent() sets correct flags */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectFlags() {

    val intent = amberSignerClient.createPublicKeyIntent()

    val expectedFlags =
        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP

    // Verify flags are included with OR operation
    assertTrue(
        "Intent should have SINGLE_TOP and CLEAR_TOP flags",
        (intent.flags and expectedFlags) == expectedFlags)
  }

  /** Test createPublicKeyIntent() sets ACTION_VIEW action */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectAction() {

    val intent = amberSignerClient.createPublicKeyIntent()

    assertEquals(
        "Intent action should be ACTION_VIEW", android.content.Intent.ACTION_VIEW, intent.action)
  }

  /** Valid response（RESULT_OK + pubkey）correct processing test */
  @Test
  fun testHandleAmberResponse_Success_ReturnsAmberResponse() {

    val pubkey = "npub1" + "a".repeat(59)
    val intent = android.content.Intent()
    intent.putExtra("result", pubkey)

    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return success", result.isSuccess)
    val response = result.getOrNull()
    assertNotNull("Response should not be null", response)
    assertEquals("Pubkey should match", pubkey, response?.pubkey)
    assertEquals(
        "PackageName should be Amber package",
        AmberSignerClient.AMBER_PACKAGE_NAME,
        response?.packageName)
  }

  /** User rejection（rejected=true）detection test */
  @Test
  fun testHandleAmberResponse_UserRejected_ReturnsError() {

    val intent = android.content.Intent()
    intent.putExtra("rejected", true)

    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is AmberError.UserRejected || error.toString().contains("UserRejected"))
  }

  /** Test RESULT_CANCELED returns UserRejected error */
  @Test
  fun testHandleAmberResponse_ResultCanceled_ReturnsUserRejected() {

    val intent = android.content.Intent()

    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_CANCELED, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is AmberError.UserRejected || error.toString().contains("UserRejected"))
  }

  /** Test null Intent returns InvalidResponse error */
  @Test
  fun testHandleAmberResponse_NullIntent_ReturnsInvalidResponse() {

    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, null)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test empty result returns InvalidResponse error */
  @Test
  fun testHandleAmberResponse_EmptyResult_ReturnsInvalidResponse() {

    val intent = android.content.Intent()
    intent.putExtra("result", "")

    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test invalid format (not starting with npub1) returns InvalidResponse error */
  @Test
  fun testHandleAmberResponse_InvalidPubkeyLength_ReturnsInvalidResponse() {

    val intent = android.content.Intent()
    intent.putExtra("result", "a".repeat(64))

    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test invalid format (starting with nsec1) returns InvalidResponse error */
  @Test
  fun testHandleAmberResponse_InvalidPubkeyFormat_ReturnsInvalidResponse() {

    val intent = android.content.Intent()
    intent.putExtra("result", "nsec1" + "a".repeat(59))

    val result = amberSignerClient.handleAmberResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test Bech32 format pubkey is masked correctly */
  @Test
  fun testMaskPubkey_ValidPubkey_ReturnsMaskedString() {

    val pubkey = "npub1" + "abcdef0123456789".repeat(3) + "abcdef01234"

    val masked = amberSignerClient.maskPubkey(pubkey)

    assertEquals("Should mask pubkey as first8...last8", "npub1abc...def01234", masked)
  }

  /** Test masking format is consistent with different pubkeys */
  @Test
  fun testMaskPubkey_DifferentPubkey_ReturnsMaskedString() {

    val pubkey = "npub1" + "1234567890abcdef".repeat(3) + "1234567890a"

    val masked = amberSignerClient.maskPubkey(pubkey)

    assertEquals("Should mask pubkey as first8...last8", "npub1123...4567890a", masked)
  }

  /** Short pubkey（less than 64 characters）masking test */
  @Test
  fun testMaskPubkey_ShortPubkey_ReturnsOriginalString() {

    val pubkey = "abcdef0123456789"

    val masked = amberSignerClient.maskPubkey(pubkey)

    assertEquals("Should return original string when pubkey is too short", pubkey, masked)
  }

  /** Empty string masking test */
  @Test
  fun testMaskPubkey_EmptyString_ReturnsEmptyString() {

    val pubkey = ""

    val masked = amberSignerClient.maskPubkey(pubkey)

    assertEquals("Should return empty string when input is empty", "", masked)
  }

  /** Test masking result length is correct */
  @Test
  fun testMaskPubkey_ResultLength_IsCorrect() {

    val pubkey = "npub1" + "a".repeat(59)

    val masked = amberSignerClient.maskPubkey(pubkey)

    assertEquals("Masked string should be 19 characters (8+3+8)", 19, masked.length)
  }
}
