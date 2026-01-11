package io.github.omochice.pinosu.data.nip55

import android.content.Context
import android.content.pm.PackageManager
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

  /** Test that returns true when NIP-55 signer is installed */
  @Test
  fun testCheckNip55SignerInstalled_WhenInstalled_ReturnsTrue() {

    every {
      packageManager.getPackageInfo(
          Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } returns android.content.pm.PackageInfo()

    val result = nip55SignerClient.checkNip55SignerInstalled()

    assertTrue("Should return true when NIP-55 signer is installed", result)
  }

  /** Test that returns false when NIP-55 signer is not installed */
  @Test
  fun testCheckNip55SignerInstalled_WhenNotInstalled_ReturnsFalse() {

    every {
      packageManager.getPackageInfo(
          Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws PackageManager.NameNotFoundException()

    val result = nip55SignerClient.checkNip55SignerInstalled()

    assertFalse("Should return false when NIP-55 signer is not installed", result)
  }

  /** Test that returns false when PackageManager throws exception */
  @Test
  fun testCheckNip55SignerInstalled_OnException_ReturnsFalse() {

    every {
      packageManager.getPackageInfo(
          Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
    } throws RuntimeException("Unexpected error")

    val result = nip55SignerClient.checkNip55SignerInstalled()

    assertFalse("Should return false on exception", result)
  }

  /** Test that Nip55Response data class is constructed correctly */
  @Test
  fun testNip55Response_Construction() {

    val pubkey = "npub1" + "a".repeat(59)
    val packageName = "com.greenart7c3.nostrsigner"

    val response = Nip55Response(pubkey, packageName)

    assertEquals("Pubkey should match", pubkey, response.pubkey)
    assertEquals("PackageName should match", packageName, response.packageName)
  }

  /** Test that Nip55Response equality is determined correctly */
  @Test
  fun testNip55Response_Equality() {

    val response1 = Nip55Response("npub1" + "abc".repeat(19) + "ab", "com.test.app")
    val response2 = Nip55Response("npub1" + "abc".repeat(19) + "ab", "com.test.app")

    assertEquals("Responses with same values should be equal", response1, response2)
  }

  /** Test Nip55Error.NotInstalled is constructed correctly */
  @Test
  fun testNip55Error_NotInstalled() {

    val error: Nip55Error = Nip55Error.NotInstalled

    assertTrue("Should be NotInstalled type", error is Nip55Error.NotInstalled)
  }

  /** Test Nip55Error.UserRejected is constructed correctly */
  @Test
  fun testNip55Error_UserRejected() {

    val error: Nip55Error = Nip55Error.UserRejected

    assertTrue("Should be UserRejected type", error is Nip55Error.UserRejected)
  }

  /** Test Nip55Error.Timeout is constructed correctly */
  @Test
  fun testNip55Error_Timeout() {

    val error: Nip55Error = Nip55Error.Timeout

    assertTrue("Should be Timeout type", error is Nip55Error.Timeout)
  }

  /** Test Nip55Error.InvalidResponse is constructed correctly */
  @Test
  fun testNip55Error_InvalidResponse() {

    val message = "Invalid response format"

    val error: Nip55Error = Nip55Error.InvalidResponse(message)

    assertTrue("Should be InvalidResponse type", error is Nip55Error.InvalidResponse)
    assertEquals("Message should match", message, (error as Nip55Error.InvalidResponse).message)
  }

  /** Test Nip55Error.IntentResolutionError is constructed correctly */
  @Test
  fun testNip55Error_IntentResolutionError() {

    val message = "Cannot resolve intent"

    val error: Nip55Error = Nip55Error.IntentResolutionError(message)

    assertTrue("Should be IntentResolutionError type", error is Nip55Error.IntentResolutionError)
    assertEquals(
        "Message should match", message, (error as Nip55Error.IntentResolutionError).message)
  }

  /** Test createPublicKeyIntent() creates Intent with correct scheme */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectScheme() {

    val intent = nip55SignerClient.createPublicKeyIntent()

    assertNotNull("Intent should have data URI", intent.data)
    assertEquals(
        "URI scheme should be nostrsigner",
        Nip55SignerClient.NOSTRSIGNER_SCHEME,
        intent.data?.scheme)
  }

  /** Test createPublicKeyIntent() sets correct package name */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectPackage() {

    val intent = nip55SignerClient.createPublicKeyIntent()

    assertEquals(
        "Package should be NIP-55 signer package name",
        Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME,
        intent.`package`)
  }

  /** Test createPublicKeyIntent() sets get_public_key type */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectType() {

    val intent = nip55SignerClient.createPublicKeyIntent()

    assertEquals(
        "Type extra should be get_public_key",
        Nip55SignerClient.TYPE_GET_PUBLIC_KEY,
        intent.getStringExtra("type"))
  }

  /** Test createPublicKeyIntent() sets correct flags */
  @Test
  fun testCreatePublicKeyIntent_HasCorrectFlags() {

    val intent = nip55SignerClient.createPublicKeyIntent()

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

    val intent = nip55SignerClient.createPublicKeyIntent()

    assertEquals(
        "Intent action should be ACTION_VIEW", android.content.Intent.ACTION_VIEW, intent.action)
  }

  /** Valid response（RESULT_OK + pubkey）correct processing test */
  @Test
  fun testHandleNip55Response_Success_ReturnsNip55Response() {

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

  /** User rejection（rejected=true）detection test */
  @Test
  fun testHandleNip55Response_UserRejected_ReturnsError() {

    val intent = android.content.Intent()
    intent.putExtra("rejected", true)

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is Nip55Error.UserRejected || error.toString().contains("UserRejected"))
  }

  /** Test RESULT_CANCELED returns UserRejected error */
  @Test
  fun testHandleNip55Response_ResultCanceled_ReturnsUserRejected() {

    val intent = android.content.Intent()

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_CANCELED, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is Nip55Error.UserRejected || error.toString().contains("UserRejected"))
  }

  /** Test null Intent returns InvalidResponse error */
  @Test
  fun testHandleNip55Response_NullIntent_ReturnsInvalidResponse() {

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, null)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test empty result returns InvalidResponse error */
  @Test
  fun testHandleNip55Response_EmptyResult_ReturnsInvalidResponse() {

    val intent = android.content.Intent()
    intent.putExtra("result", "")

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test invalid format (not starting with npub1) returns InvalidResponse error */
  @Test
  fun testHandleNip55Response_InvalidPubkeyLength_ReturnsInvalidResponse() {

    val intent = android.content.Intent()
    intent.putExtra("result", "a".repeat(64))

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test invalid format (starting with nsec1) returns InvalidResponse error */
  @Test
  fun testHandleNip55Response_InvalidPubkeyFormat_ReturnsInvalidResponse() {

    val intent = android.content.Intent()
    intent.putExtra("result", "nsec1" + "a".repeat(59))

    val result = nip55SignerClient.handleNip55Response(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test Bech32 format pubkey is masked correctly */
  @Test
  fun testMaskPubkey_ValidPubkey_ReturnsMaskedString() {

    val pubkey = "npub1" + "abcdef0123456789".repeat(3) + "abcdef01234"

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Should mask pubkey as first8...last8", "npub1abc...def01234", masked)
  }

  /** Test masking format is consistent with different pubkeys */
  @Test
  fun testMaskPubkey_DifferentPubkey_ReturnsMaskedString() {

    val pubkey = "npub1" + "1234567890abcdef".repeat(3) + "1234567890a"

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Should mask pubkey as first8...last8", "npub1123...4567890a", masked)
  }

  /** Short pubkey（less than 64 characters）masking test */
  @Test
  fun testMaskPubkey_ShortPubkey_ReturnsOriginalString() {

    val pubkey = "abcdef0123456789"

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Should return original string when pubkey is too short", pubkey, masked)
  }

  /** Empty string masking test */
  @Test
  fun testMaskPubkey_EmptyString_ReturnsEmptyString() {

    val pubkey = ""

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Should return empty string when input is empty", "", masked)
  }

  /** Test masking result length is correct */
  @Test
  fun testMaskPubkey_ResultLength_IsCorrect() {

    val pubkey = "npub1" + "a".repeat(59)

    val masked = nip55SignerClient.maskPubkey(pubkey)

    assertEquals("Masked string should be 19 characters (8+3+8)", 19, masked.length)
  }

  /** Test createGetRelaysIntent() creates Intent with correct type */
  @Test
  fun testCreateGetRelaysIntent_HasCorrectType() {

    val intent = nip55SignerClient.createGetRelaysIntent()

    assertEquals(
        "Type extra should be get_relays",
        Nip55SignerClient.TYPE_GET_RELAYS,
        intent.getStringExtra("type"))
  }

  /** Test createGetRelaysIntent() creates Intent with correct scheme */
  @Test
  fun testCreateGetRelaysIntent_HasCorrectScheme() {

    val intent = nip55SignerClient.createGetRelaysIntent()

    assertNotNull("Intent should have data URI", intent.data)
    assertEquals(
        "URI scheme should be nostrsigner",
        Nip55SignerClient.NOSTRSIGNER_SCHEME,
        intent.data?.scheme)
  }

  /** Test createGetRelaysIntent() sets correct package name */
  @Test
  fun testCreateGetRelaysIntent_HasCorrectPackage() {

    val intent = nip55SignerClient.createGetRelaysIntent()

    assertEquals(
        "Package should be NIP-55 signer package name",
        Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME,
        intent.`package`)
  }

  /** Test createGetRelaysIntent() sets correct flags */
  @Test
  fun testCreateGetRelaysIntent_HasCorrectFlags() {

    val intent = nip55SignerClient.createGetRelaysIntent()

    val expectedFlags =
        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP

    assertTrue(
        "Intent should have SINGLE_TOP and CLEAR_TOP flags",
        (intent.flags and expectedFlags) == expectedFlags)
  }

  /** Test createGetRelaysIntent() sets ACTION_VIEW action */
  @Test
  fun testCreateGetRelaysIntent_HasCorrectAction() {

    val intent = nip55SignerClient.createGetRelaysIntent()

    assertEquals(
        "Intent action should be ACTION_VIEW", android.content.Intent.ACTION_VIEW, intent.action)
  }

  /** Test handleRelayListResponse() parses valid JSON response */
  @Test
  fun testHandleRelayListResponse_ValidJson_ReturnsRelayConfigList() {

    val json =
        """{"wss://relay1.example.com": {"read": true, "write": true}, "wss://relay2.example.com": {"read": true, "write": false}}"""
    val intent = android.content.Intent()
    intent.putExtra("result", json)

    val result = nip55SignerClient.handleRelayListResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return success", result.isSuccess)
    val relays = result.getOrNull()
    assertNotNull("Relays should not be null", relays)
    assertEquals("Should have 2 relays", 2, relays?.size)

    val relay1 = relays?.find { it.url == "wss://relay1.example.com" }
    assertNotNull("Should contain relay1", relay1)
    assertTrue("relay1 should have read=true", relay1?.read == true)
    assertTrue("relay1 should have write=true", relay1?.write == true)

    val relay2 = relays?.find { it.url == "wss://relay2.example.com" }
    assertNotNull("Should contain relay2", relay2)
    assertTrue("relay2 should have read=true", relay2?.read == true)
    assertFalse("relay2 should have write=false", relay2?.write == true)
  }

  /** Test handleRelayListResponse() handles empty relay list */
  @Test
  fun testHandleRelayListResponse_EmptyJson_ReturnsEmptyList() {

    val json = "{}"
    val intent = android.content.Intent()
    intent.putExtra("result", json)

    val result = nip55SignerClient.handleRelayListResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return success", result.isSuccess)
    val relays = result.getOrNull()
    assertNotNull("Relays should not be null", relays)
    assertTrue("Should have empty relay list", relays?.isEmpty() == true)
  }

  /** Test handleRelayListResponse() filters read-only relays */
  @Test
  fun testHandleRelayListResponse_FiltersReadOnlyRelays() {

    val json =
        """{"wss://read-relay.example.com": {"read": true, "write": false}, "wss://write-only.example.com": {"read": false, "write": true}}"""
    val intent = android.content.Intent()
    intent.putExtra("result", json)

    val result = nip55SignerClient.handleRelayListResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return success", result.isSuccess)
    val relays = result.getOrNull()
    assertNotNull("Relays should not be null", relays)
    assertEquals("Should have 1 relay (only read=true)", 1, relays?.size)
    assertEquals(
        "Should only contain read-enabled relay",
        "wss://read-relay.example.com",
        relays?.first()?.url)
  }

  /** Test handleRelayListResponse() returns error on invalid JSON */
  @Test
  fun testHandleRelayListResponse_InvalidJson_ReturnsError() {

    val invalidJson = "not a json"
    val intent = android.content.Intent()
    intent.putExtra("result", invalidJson)

    val result = nip55SignerClient.handleRelayListResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test handleRelayListResponse() returns error on RESULT_CANCELED */
  @Test
  fun testHandleRelayListResponse_ResultCanceled_ReturnsUserRejected() {

    val intent = android.content.Intent()

    val result =
        nip55SignerClient.handleRelayListResponse(android.app.Activity.RESULT_CANCELED, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is Nip55Error.UserRejected || error.toString().contains("UserRejected"))
  }

  /** Test handleRelayListResponse() returns error on null intent */
  @Test
  fun testHandleRelayListResponse_NullIntent_ReturnsError() {

    val result = nip55SignerClient.handleRelayListResponse(android.app.Activity.RESULT_OK, null)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be InvalidResponse",
        error is Nip55Error.InvalidResponse || error.toString().contains("InvalidResponse"))
  }

  /** Test handleRelayListResponse() handles missing read/write fields with defaults */
  @Test
  fun testHandleRelayListResponse_MissingFields_UsesDefaults() {

    val json = """{"wss://relay.example.com": {}}"""
    val intent = android.content.Intent()
    intent.putExtra("result", json)

    val result = nip55SignerClient.handleRelayListResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return success", result.isSuccess)
    val relays = result.getOrNull()
    assertNotNull("Relays should not be null", relays)
    assertEquals("Should have 1 relay", 1, relays?.size)
    val relay = relays?.first()
    assertTrue("read should default to true", relay?.read == true)
    assertTrue("write should default to true", relay?.write == true)
  }

  /** Test handleRelayListResponse() handles rejected extra */
  @Test
  fun testHandleRelayListResponse_RejectedExtra_ReturnsUserRejected() {

    val intent = android.content.Intent()
    intent.putExtra("rejected", true)

    val result = nip55SignerClient.handleRelayListResponse(android.app.Activity.RESULT_OK, intent)

    assertTrue("Should return failure", result.isFailure)
    val error = result.exceptionOrNull()
    assertTrue(
        "Error should be UserRejected",
        error is Nip55Error.UserRejected || error.toString().contains("UserRejected"))
  }
}
