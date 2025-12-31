package io.github.omochice.pinosu.data.amber

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.domain.model.isValidNostrPubkey
import javax.inject.Inject
import javax.inject.Singleton

/** Encapsulation of Amber NIP-55 Intent communication */
@Singleton
class AmberSignerClient @Inject constructor(@ApplicationContext private val context: Context) {

  /**
   * Check if Amber app is installed
   *
   * @return true if Amber is installed, false otherwise
   */
  fun checkAmberInstalled(): Boolean {
    return try {
      context.packageManager.getPackageInfo(AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
      true
    } catch (e: PackageManager.NameNotFoundException) {
      false
    } catch (e: Exception) {
      false
    }
  }

  /**
   * Create Intent for NIP-55 public key retrieval request
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
   * @param resultCode ActivityResult's resultCode (RESULT_OK or RESULT_CANCELED)
   * @param data Intent data (containing result and rejected)
   * @return Success(AmberResponse) or Failure(AmberError)
   */
  fun handleAmberResponse(resultCode: Int, data: android.content.Intent?): Result<AmberResponse> {
    if (resultCode == android.app.Activity.RESULT_CANCELED) {
      return Result.failure(AmberError.UserRejected)
    }

    if (data == null) {
      return Result.failure(AmberError.InvalidResponse("Intent data is null"))
    }

    val rejected = data.getBooleanExtra("rejected", false)
    if (rejected) {
      return Result.failure(AmberError.UserRejected)
    }

    val pubkey = data.getStringExtra("result")
    if (pubkey.isNullOrEmpty()) {
      return Result.failure(AmberError.InvalidResponse("Result is null or empty"))
    }

    if (!pubkey.isValidNostrPubkey()) {
      return Result.failure(
          AmberError.InvalidResponse("Invalid pubkey format: must be Bech32-encoded (npub1...)"))
    }

    return Result.success(AmberResponse(pubkey, AMBER_PACKAGE_NAME))
  }

  /**
   * Mask sensitive pubkey for logging
   *
   * @param pubkey pubkey string to mask
   * @return Masked pubkey string (e.g., "abcd1234...wxyz7890")
   */
  fun maskPubkey(pubkey: String): String {
    if (pubkey.isEmpty()) {
      return pubkey
    }

    // Reason: masking result would be 19 characters (8+3+8), which increases information for
    // strings 16 chars or less
    if (pubkey.length <= 16) {
      return pubkey
    }

    val prefix = pubkey.take(8)
    val suffix = pubkey.takeLast(8)
    return "$prefix...$suffix"
  }

  companion object {
    const val AMBER_PACKAGE_NAME = "com.greenart7c3.nostrsigner"
    const val NOSTRSIGNER_SCHEME = "nostrsigner"
    const val TYPE_GET_PUBLIC_KEY = "get_public_key"
  }
}

/**
 * Response from Amber
 *
 * @property pubkey User's public key (64-character hex string)
 * @property packageName Amber app package name
 */
data class AmberResponse(val pubkey: String, val packageName: String)

/** Amber communication errors */
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
