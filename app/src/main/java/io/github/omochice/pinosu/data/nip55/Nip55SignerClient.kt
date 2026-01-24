package io.github.omochice.pinosu.data.nip55

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.domain.model.isValidNostrPubkey
import javax.inject.Inject
import javax.inject.Singleton

/** Encapsulation of NIP-55 Intent communication */
@Singleton
class Nip55SignerClient
@Inject
constructor(@param:ApplicationContext private val context: Context) {

  /**
   * Check if NIP-55 signer app is installed
   *
   * @return true if NIP-55 signer is installed, false otherwise
   */
  fun checkNip55SignerInstalled(): Boolean {
    return try {
      context.packageManager.getPackageInfo(
          NIP55_SIGNER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
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
  fun createPublicKeyIntent(): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$NOSTRSIGNER_SCHEME:"))
    intent.`package` = NIP55_SIGNER_PACKAGE_NAME
    intent.putExtra("type", TYPE_GET_PUBLIC_KEY)
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    return intent
  }

  /**
   * Handle ActivityResult response from NIP-55 signer
   *
   * @param resultCode ActivityResult's resultCode (RESULT_OK or RESULT_CANCELED)
   * @param data Intent data (containing result and rejected)
   * @return Success(Nip55Response) or Failure(Nip55Error)
   */
  fun handleNip55Response(resultCode: Int, data: Intent?): Result<Nip55Response> {
    if (resultCode == Activity.RESULT_CANCELED) {
      return Result.failure(Nip55Error.UserRejected)
    }

    data ?: return Result.failure(Nip55Error.InvalidResponse("Intent data is null"))

    val rejected = data.getBooleanExtra("rejected", false)
    if (rejected) {
      return Result.failure(Nip55Error.UserRejected)
    }

    val pubkey = data.getStringExtra("result")
    if (pubkey.isNullOrEmpty()) {
      return Result.failure(Nip55Error.InvalidResponse("Result is null or empty"))
    }

    if (!pubkey.isValidNostrPubkey()) {
      return Result.failure(
          Nip55Error.InvalidResponse("Invalid pubkey format: must be Bech32-encoded (npub1...)"))
    }

    return Result.success(Nip55Response(pubkey, NIP55_SIGNER_PACKAGE_NAME))
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

  /**
   * Create Intent for NIP-55 event signing request
   *
   * @param unsignedEventJson JSON string of the unsigned event
   * @return Constructed Intent for signing
   */
  fun createSignEventIntent(unsignedEventJson: String): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$NOSTRSIGNER_SCHEME:$unsignedEventJson"))
    intent.`package` = NIP55_SIGNER_PACKAGE_NAME
    intent.putExtra("type", TYPE_SIGN_EVENT)
    intent.putExtra("returnType", RETURN_TYPE_EVENT)
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    return intent
  }

  /**
   * Handle ActivityResult response from NIP-55 signer for sign event
   *
   * @param resultCode ActivityResult's resultCode (RESULT_OK or RESULT_CANCELED)
   * @param data Intent data (containing signature)
   * @return Success(SignedEventResponse) or Failure(Nip55Error)
   */
  fun handleSignEventResponse(resultCode: Int, data: Intent?): Result<SignedEventResponse> {
    if (resultCode == Activity.RESULT_CANCELED) {
      return Result.failure(Nip55Error.UserRejected)
    }

    data ?: return Result.failure(Nip55Error.InvalidResponse("Intent data is null"))

    val rejected = data.getBooleanExtra("rejected", false)
    if (rejected) {
      return Result.failure(Nip55Error.UserRejected)
    }

    val signedEventJson = data.getStringExtra("result")
    Log.d(TAG, "Sign event response: $signedEventJson")

    if (signedEventJson.isNullOrEmpty()) {
      return Result.failure(Nip55Error.InvalidResponse("Signed event result is null or empty"))
    }

    return Result.success(SignedEventResponse(signedEventJson))
  }

  companion object {
    private const val TAG = "Nip55SignerClient"
    const val NIP55_SIGNER_PACKAGE_NAME = "com.greenart7c3.nostrsigner"
    const val NOSTRSIGNER_SCHEME = "nostrsigner"
    const val TYPE_GET_PUBLIC_KEY = "get_public_key"
    const val TYPE_SIGN_EVENT = "sign_event"
    const val RETURN_TYPE_EVENT = "event"
  }
}

/**
 * Response from NIP-55 signer for sign event request
 *
 * @property signedEventJson Signed event as JSON string (contains id and sig)
 */
data class SignedEventResponse(val signedEventJson: String)

/**
 * Response from NIP-55 signer
 *
 * @property pubkey User's public key (64-character hex string)
 * @property packageName NIP-55 signer app package name
 */
data class Nip55Response(val pubkey: String, val packageName: String)

/** NIP-55 signer communication errors */
sealed class Nip55Error : Exception() {
  /** NIP-55 signer app is not installed */
  data object NotInstalled : Nip55Error()

  /** User rejected operation in NIP-55 signer */
  data object UserRejected : Nip55Error()

  /** Response from NIP-55 signer timed out */
  data object Timeout : Nip55Error()

  /**
   * Response from NIP-55 signer was in invalid format
   *
   * @property message Error message
   */
  data class InvalidResponse(override val message: String) : Nip55Error()

  /**
   * Intent resolution failed
   *
   * @property message Error message
   */
  data class IntentResolutionError(override val message: String) : Nip55Error()
}
