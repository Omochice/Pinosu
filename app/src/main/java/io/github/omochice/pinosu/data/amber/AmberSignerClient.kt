package io.github.omochice.pinosu.data.amber

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsulation of Amber NIP-55 Intent communication
 *
 * Handles communication with Amber app based on NIP-55 protocol. Task 4.1: AmberSignerClient basic
 * implementation Requirements: 1.2, 5.1
 */
@Singleton
class AmberSignerClient @Inject constructor(@ApplicationContext private val context: Context) {

  /**
   * Check if Amber app is installed
   *
   * Verifies Amber package existence using PackageManager.
   *
   * Task 4.1: checkAmberInstalled() implementation Requirement 1.2: Amber uninstalled detection
   *
   * @return true if Amber is installed, false otherwise
   */
  fun checkAmberInstalled(): Boolean {
    return try {
      context.packageManager.getPackageInfo(AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
      true
    } catch (e: PackageManager.NameNotFoundException) {
      // Amber is not installed
      false
    } catch (e: Exception) {
      // Treat other exceptions (security exceptions, etc.) as false
      false
    }
  }

  /**
   * Create Intent for NIP-55 public key retrieval request
   *
   * Constructs Intent compliant with NIP-55 protocol. Uses nostrsigner: scheme to send
   * get_public_key request to Amber app.
   *
   * Task 4.2: createPublicKeyIntent() implementation Requirements: 1.3, 4.1, 4.2
   *
   * @return Constructed Intent
   */
  fun createPublicKeyIntent(): android.content.Intent {
    val intent =
        android.content.Intent(
            android.content.Intent.ACTION_VIEW, android.net.Uri.parse("$NOSTRSIGNER_SCHEME:"))
    intent.`package` = AMBER_PACKAGE_NAME
    intent.putExtra("type", TYPE_GET_PUBLIC_KEY)
    intent.addFlags(
        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
    return intent
  }

  /**
   * Handle ActivityResult response from Amber
   *
   * Parses Amber response based on NIP-55 protocol and determines success/failure.
   *
   * Task 4.3: handleAmberResponse implementation Requirements: 1.3, 1.5, 4.5, 5.3, 5.4
   *
   * @param resultCode ActivityResult's resultCode (RESULT_OK or RESULT_CANCELED)
   * @param data Intent data (containing result and rejected)
   * @return Success(AmberResponse) or Failure(AmberError)
   */
  fun handleAmberResponse(resultCode: Int, data: android.content.Intent?): Result<AmberResponse> {
    // RESULT_CANCELED indicates user rejection
    if (resultCode == android.app.Activity.RESULT_CANCELED) {
      return Result.failure(AmberError.UserRejected)
    }

    // Null Intent indicates invalid response
    if (data == null) {
      return Result.failure(AmberError.InvalidResponse("Intent data is null"))
    }

    // Check rejected flag
    val rejected = data.getBooleanExtra("rejected", false)
    if (rejected) {
      return Result.failure(AmberError.UserRejected)
    }

    // Get pubkey from result
    val pubkey = data.getStringExtra("result")
    if (pubkey.isNullOrEmpty()) {
      return Result.failure(AmberError.InvalidResponse("Result is null or empty"))
    }

    // Validate pubkey format (64 hex characters)
    if (!isValidPubkey(pubkey)) {
      return Result.failure(
          AmberError.InvalidResponse("Invalid pubkey format: must be 64 hex characters"))
    }

    return Result.success(AmberResponse(pubkey, AMBER_PACKAGE_NAME))
  }

  /**
   * Validate pubkey format
   *
   * @param pubkey pubkey string to validate
   * @return true if 64-character hex string, false otherwise
   */
  private fun isValidPubkey(pubkey: String): Boolean {
    if (pubkey.length != 64) return false
    return pubkey.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
  }

  /**
   * Mask sensitive pubkey for logging
   *
   * Masks 64-character pubkey in format "first 8 chars + ... + last 8 chars". Returns original
   * string if less than 16 characters.
   *
   * Task 4.4: Sensitive data log masking Requirement 6.3: Sensitive data masking
   *
   * @param pubkey pubkey string to mask
   * @return Masked pubkey string (e.g., "abcd1234...wxyz7890")
   */
  fun maskPubkey(pubkey: String): String {
    // Return as-is for empty string
    if (pubkey.isEmpty()) {
      return pubkey
    }

    // Return original string without masking if 16 characters or less
    // Reason: masking result would be 19 characters (8+3+8), which increases information for
    // strings 16 chars or less
    if (pubkey.length <= 16) {
      return pubkey
    }

    // Mask in format: first 8 chars + "..." + last 8 chars
    val prefix = pubkey.take(8)
    val suffix = pubkey.takeLast(8)
    return "$prefix...$suffix"
  }

  companion object {
    /** Amber app package name */
    const val AMBER_PACKAGE_NAME = "com.greenart7c3.nostrsigner"

    /** NIP-55 Intent scheme */
    const val NOSTRSIGNER_SCHEME = "nostrsigner"

    /** get_public_key request type */
    const val TYPE_GET_PUBLIC_KEY = "get_public_key"
  }
}

/**
 * Response from Amber
 *
 * Task 4.1: AmberResponse data class definition
 *
 * @property pubkey User's public key (64-character hex string)
 * @property packageName Amber app package name
 */
data class AmberResponse(val pubkey: String, val packageName: String)

/**
 * Amber communication errors
 *
 * Represents errors that can occur during NIP-55 Intent communication with Amber app. Task 4.1:
 * AmberError definition Requirements: 1.5, 4.5, 5.1, 5.3, 5.4
 */
sealed class AmberError : Exception() {
  /** Amber app is not installed */
  data object NotInstalled : AmberError()

  /** User rejected operation in Amber */
  data object UserRejected : AmberError()

  /** Response from Amber timed out */
  data object Timeout : AmberError()

  /**
   * Response from Amber was in invalid format
   *
   * @property message Error message
   */
  data class InvalidResponse(override val message: String) : AmberError()

  /**
   * Intent resolution failed
   *
   * @property message Error message
   */
  data class IntentResolutionError(override val message: String) : AmberError()
}
