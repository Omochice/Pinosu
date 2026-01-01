package io.github.omochice.pinosu.data.amber

import roid.content.Context
import roid.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*import org.junit.Before
import org.junit.test
import org.junit.runner.RunWith
import org.robolectric.RobolectrictestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
class AmberSignerClienttest {

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

// ========== checkAmberInstalled() tests ==========
 fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {
// Given: Ambereding every {
 packageManager.getPackageInfo(
 AmberSignerClient.AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
 } returns roid.content.pm.PackageInfo()

// When: Call checkAmberInstalled() val result = amberSignerClient.checkAmberInstalled()

// Then: true is returned assertTrue("Should return true when Amber is installed", result)
 }

 fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {
// Given: Amberednot (PackageManager.NameNotFoundException) every {
 packageManager.getPackageInfo(
 AmberSignerClient.AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
 } throws PackageManager.NameNotFoundException()

// When: Call checkAmberInstalled() val result = amberSignerClient.checkAmberInstalled()

// Then: false is returned assertFalse("Should return false when Amber is not installed", result)
 }

 fun testCheckAmberInstalled_OnException_ReturnsFalse() {
// Given: PackageManagerthrow exception every {
 packageManager.getPackageInfo(
 AmberSignerClient.AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
 } throws RuntimeException("Unexpected error")

// When: Call checkAmberInstalled() val result = amberSignerClient.checkAmberInstalled()

// Then: false is returned assertFalse("Should return false on exception", result)
 }

// ========== AmberResponse Data Class tests ==========
 fun testAmberResponse_Construction() {
// Given: Validpubkey packageName val pubkey = "npub1" + "a".repeat(59)
 val packageName = "com.greenart7c3.nostrsigner"

// When: Construct AmberResponse val response = AmberResponse(pubkey, packageName)

// Then: is correctlyseted assertEquals("Pubkey should match", pubkey, response.pubkey)
 assertEquals("PackageName should match", packageName, response.packageName)
 }

 fun testAmberResponse_Equality() {
// Given: Two with the same valueAmberResponse val response1 = AmberResponse("npub1" + "abc".repeat(19) + "ab", "com.test.app")
 val response2 = AmberResponse("npub1" + "abc".repeat(19) + "ab", "com.test.app")

// Then: a assertEquals("Responses with same values should be equal", response1, response2)
 }

// ========== AmberError Sealed Class tests ==========
 fun testAmberError_NotInstalled() {
// When: NotInstallederror val error: AmberError = AmberError.NotInstalled

// Then: Correcttype assertTrue("Should be NotInstalled type", error is AmberError.NotInstalled)
 }

 fun testAmberError_UserRejected() {
// When: UserRejectederror val error: AmberError = AmberError.UserRejected

// Then: Correcttype assertTrue("Should be UserRejected type", error is AmberError.UserRejected)
 }

 fun testAmberError_Timeout() {
// When: Timeouterror val error: AmberError = AmberError.Timeout

// Then: Correcttype assertTrue("Should be Timeout type", error is AmberError.Timeout)
 }

 fun testAmberError_InvalidResponse() {
// Given: Error message val message = "Invalid response format"

// When: InvalidResponseerror val error: AmberError = AmberError.InvalidResponse(message)

// Then: Correcttype message assertTrue("Should be InvalidResponse type", error is AmberError.InvalidResponse)
 assertEquals("Message should match", message, (error as AmberError.InvalidResponse).message)
 }

 fun testAmberError_IntentResolutionError() {
// Given: Error message val message = "Cannot resolve intent"

// When: IntentResolutionErrorerror val error: AmberError = AmberError.IntentResolutionError(message)

// Then: Correcttype message assertTrue("Should be IntentResolutionError type", error is AmberError.IntentResolutionError)
 assertEquals(
 "Message should match", message, (error as AmberError.IntentResolutionError).message)
 }

// ========== createPublicKeyIntent() tests ==========
 fun testCreatePublicKeyIntent_HasCorrectScheme() {
// When: Call createPublicKeyIntent() val intent = amberSignerClient.createPublicKeyIntent()

// Then: nostrsignerkeyofURI assertNotNull("Intent should have data URI", intent.data)
 assertEquals(
 "URI scheme should be nostrsigner",
 AmberSignerClient.NOSTRSIGNER_SCHEME,
 intent.data?.scheme)
 }

 fun testCreatePublicKeyIntent_HasCorrectPackage() {
// When: Call createPublicKeyIntent() val intent = amberSignerClient.createPublicKeyIntent()

// Then: Amberofseteding assertEquals(
 "Package should be Amber package name",
 AmberSignerClient.AMBER_PACKAGE_NAME,
 intent.`package`)
 }

 fun testCreatePublicKeyIntent_HasCorrectType() {
// When: Call createPublicKeyIntent() val intent = amberSignerClient.createPublicKeyIntent()

// Then: type extraget_public_keyseting assertEquals(
 "Type extra should be get_public_key",
 AmberSignerClient.TYPE_GET_PUBLIC_KEY,
 intent.getStringExtra("type"))
 }

 fun testCreatePublicKeyIntent_HasCorrectFlags() {
// When: Call createPublicKeyIntent() val intent = amberSignerClient.createPublicKeyIntent()

// Then: SINGLE_TOP CLEAR_TOPflagseting val expectedFlags =
 roid.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
 roid.content.Intent.FLAG_ACTIVITY_CLEAR_TOP

// flagVerify that flags are included with OR operation assertTrue(
 "Intent should have SINGLE_TOP CLEAR_TOP flags",
 (intent.flags expectedFlags) == expectedFlags)
 }

 fun testCreatePublicKeyIntent_HasCorrectAction() {
// When: Call createPublicKeyIntent() val intent = amberSignerClient.createPublicKeyIntent()

// Then: ACTION_VIEWseteding assertEquals(
 "Intent action should be ACTION_VIEW", roid.content.Intent.ACTION_VIEW, intent.action)
 }

// ========== h leAmberResponse() tests ==========
 fun testH leAmberResponse_Success_ReturnsAmberResponse() {
// Given: RESULT_OK valid pubkeyIntent val pubkey = "npub1" + "a".repeat(59)
 val intent = roid.content.Intent()
 intent.putExtra("result", pubkey)

// When: Call h leAmberResponse() val result = amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: Successed, pubkey packageName assertTrue("Should return success", result.isSuccess)
 val response = result.getOrNull()
 assertNotNull("Response should not be null", response)
 assertEquals("Pubkey should match", pubkey, response?.pubkey)
 assertEquals(
 "PackageName should be Amber package",
 AmberSignerClient.AMBER_PACKAGE_NAME,
 response?.packageName)
 }

 fun testH leAmberResponse_UserRejected_ReturnsError() {
// Given: Intent with rejected=true val intent = roid.content.Intent()
 intent.putExtra("rejected", true)

// When: Call h leAmberResponse() val result = amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: UserRejectederroris returned assertTrue("Should return failure", result.isFailure)
 val error = result.exceptionOrNull()
 assertTrue(
 "Error should be UserRejected",
 error is AmberError.UserRejected || error.toString().contains("UserRejected"))
 }

 fun testH leAmberResponse_ResultCanceled_ReturnsUserRejected() {
// Given: RESULT_CANCELED val intent = roid.content.Intent()

// When: Call h leAmberResponse() val result = amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_CANCELED, intent)

// Then: UserRejectederroris returned assertTrue("Should return failure", result.isFailure)
 val error = result.exceptionOrNull()
 assertTrue(
 "Error should be UserRejected",
 error is AmberError.UserRejected || error.toString().contains("UserRejected"))
 }

 fun testH leAmberResponse_NullIntent_ReturnsInvalidResponse() {
// Given: null Intent// When: Call h leAmberResponse() val result = amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, null)

// Then: InvalidResponseerroris returned assertTrue("Should return failure", result.isFailure)
 val error = result.exceptionOrNull()
 assertTrue(
 "Error should be InvalidResponse",
 error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
 }

 fun testH leAmberResponse_EmptyResult_ReturnsInvalidResponse() {
// Given: Empty result val intent = roid.content.Intent()
 intent.putExtra("result", "")

// When: Call h leAmberResponse() val result = amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: InvalidResponseerroris returned assertTrue("Should return failure", result.isFailure)
 val error = result.exceptionOrNull()
 assertTrue(
 "Error should be InvalidResponse",
 error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
 }

 fun testH leAmberResponse_InvalidPubkeyLength_ReturnsInvalidResponse() {
// Given: Invalidformatofpubkey (npub1does not start with) val intent = roid.content.Intent()
 intent.putExtra("result", "a".repeat(64))

// When: Call h leAmberResponse() val result = amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: InvalidResponseerroris returned assertTrue("Should return failure", result.isFailure)
 val error = result.exceptionOrNull()
 assertTrue(
 "Error should be InvalidResponse",
 error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
 }

 fun testH leAmberResponse_InvalidPubkeyFormat_ReturnsInvalidResponse() {
// Given: Private key format pubkey (nsec1starts with) val intent = roid.content.Intent()
 intent.putExtra("result", "nsec1" + "a".repeat(59))

// When: Call h leAmberResponse() val result = amberSignerClient.h leAmberResponse( roid.app.Activity.RESULT_OK, intent)

// Then: InvalidResponseerroris returned assertTrue("Should return failure", result.isFailure)
 val error = result.exceptionOrNull()
 assertTrue(
 "Error should be InvalidResponse",
 error is AmberError.InvalidResponse || error.toString().contains("InvalidResponse"))
 }

// ========== maskPubkey() tests ==========
 fun testMaskPubkey_ValidPubkey_ReturnsMaskedString() {
// Given: Bech32 format pubkey val pubkey = "npub1" + "abcdef0123456789".repeat(3) + "abcdef01234"

// When: Call maskPubkey() val masked = amberSignerClient.maskPubkey(pubkey)

// Then: first 8 characters + "..." + last 8 charactersofformated assertEquals("Should mask pubkey as first8...last8", "npub1abc...def01234", masked)
 }

 fun testMaskPubkey_DifferentPubkey_ReturnsMaskedString() {
// Given: Different ofBech32 formatofpubkey val pubkey = "npub1" + "1234567890abcdef".repeat(3) + "1234567890a"

// When: Call maskPubkey() val masked = amberSignerClient.maskPubkey(pubkey)

// Then: first 8 characters + "..." + last 8 charactersofformated assertEquals("Should mask pubkey as first8...last8", "npub1123...4567890a", masked)
 }

 fun testMaskPubkey_ShortPubkey_ReturnsOriginalString() {
// Given: Short pubkey with 16 characters or less val pubkey = "abcdef0123456789"

// When: Call maskPubkey() val masked = amberSignerClient.maskPubkey(pubkey)

// Then: ofstring assertEquals("Should return original string when pubkey is too short", pubkey, masked)
 }

 fun testMaskPubkey_EmptyString_ReturnsEmptyString() {
// Given: Empty string val pubkey = ""

// When: Call maskPubkey() val masked = amberSignerClient.maskPubkey(pubkey)

// Then: empty string assertEquals("Should return empty string when input is empty", "", masked)
 }

 fun testMaskPubkey_ResultLength_IsCorrect() {
// Given: Bech32 format pubkey val pubkey = "npub1" + "a".repeat(59)

// When: Call maskPubkey() val masked = amberSignerClient.maskPubkey(pubkey)

// Then: result19 (8 + 3 + 8)a assertEquals("Masked string should be 19 characters (8+3+8)", 19, masked.length)
 }
}
