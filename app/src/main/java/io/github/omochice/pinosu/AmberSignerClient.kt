 package io.github.omochice.pinosu.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * Minimal Amber signer client skeleton implementing NIP-55 intent interactions.
 * This is a non-invasive starting point; wiring with ActivityResultLauncher and
 * persistence will be implemented in follow-up tasks.
 */
interface AmberSignerClient {
    fun isAmberInstalled(context: Context): Boolean
    fun buildGetPublicKeyIntent(): Intent
    fun parseGetPublicKeyResult(resultCode: Int, data: Intent?): Result<String>
}

object AmberSignerClientImpl : AmberSignerClient {
    private const val AMBER_PACKAGE = "com.greenart7c3.nostrsigner"

    override fun isAmberInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(AMBER_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun buildGetPublicKeyIntent(): Intent {
        val uri = Uri.parse("nostrsigner:")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            `package` = AMBER_PACKAGE
            putExtra("type", "get_public_key")
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    override fun parseGetPublicKeyResult(resultCode: Int, data: Intent?): Result<String> {
        if (resultCode == Activity.RESULT_OK) {
            val pubkey = data?.getStringExtra("result")
            return if (!pubkey.isNullOrBlank()) {
                Result.success(pubkey)
            } else {
                Result.failure(IllegalStateException("invalid_response"))
            }
        }

        if (resultCode == Activity.RESULT_CANCELED) {
            return Result.failure(IllegalStateException("user_rejected"))
        }

        return Result.failure(IllegalStateException("unexpected_result_code:$resultCode"))
    }
}
