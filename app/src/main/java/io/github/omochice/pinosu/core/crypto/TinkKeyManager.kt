package io.github.omochice.pinosu.core.crypto

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Tink AEAD encryption keys using Android Keystore
 *
 * Provides AES256-GCM encryption through Tink library, with keys stored securely in Android
 * Keystore. The keyset is created on first access and persisted in SharedPreferences (encrypted by
 * Android Keystore master key).
 */
@Singleton
class TinkKeyManager @Inject constructor(@param:ApplicationContext private val context: Context) {

  private val keysetHandle: KeysetHandle by lazy { createOrLoadKeysetHandle() }

  init {
    AeadConfig.register()
  }

  /**
   * Get AEAD primitive for encryption/decryption
   *
   * @return Aead primitive configured with AES256-GCM
   */
  fun getAead(): Aead = keysetHandle.getPrimitive(Aead::class.java)

  private fun createOrLoadKeysetHandle(): KeysetHandle =
      AndroidKeysetManager.Builder()
          .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
          .withKeyTemplate(KeyTemplates.get(AES256_GCM_TEMPLATE))
          .withMasterKeyUri(MASTER_KEY_URI)
          .build()
          .keysetHandle

  companion object {
    private const val KEYSET_NAME = "pinosu_auth_keyset"
    private const val PREF_FILE_NAME = "pinosu_tink_keyset_prefs"
    private const val MASTER_KEY_URI = "android-keystore://pinosu_master_key"
    private const val AES256_GCM_TEMPLATE = "AES256_GCM"
  }
}
