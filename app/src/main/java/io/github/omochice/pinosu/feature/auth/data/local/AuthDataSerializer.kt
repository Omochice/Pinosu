package io.github.omochice.pinosu.feature.auth.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * DataStore Serializer for AuthData with Tink AEAD encryption
 *
 * Encrypts data using AES256-GCM before writing to disk and decrypts on read. Uses associated data
 * for additional authenticity verification.
 */
class AuthDataSerializer(private val aead: Aead) : Serializer<AuthData> {

  override val defaultValue: AuthData = AuthData.DEFAULT

  override suspend fun readFrom(input: InputStream): AuthData {
    val encryptedBytes = input.readBytes()
    if (encryptedBytes.isEmpty()) {
      return defaultValue
    }

    // Non-empty data that fails to decrypt or deserialize is genuinely corrupt or currently
    // unreadable (e.g. a rotated Tink keyset or a temporarily inaccessible keystore key). Returning
    // defaultValue here would be indistinguishable from a fresh install and would silently discard
    // the still-encrypted data on disk, so surface it as a CorruptionException instead. The file is
    // left untouched so the data can still be recovered once the key becomes readable again.
    return try {
      val decryptedBytes = aead.decrypt(encryptedBytes, ASSOCIATED_DATA)
      Json.decodeFromString<AuthData>(decryptedBytes.decodeToString())
    } catch (e: GeneralSecurityException) {
      throw CorruptionException("Failed to decrypt stored auth data", e)
    } catch (e: SerializationException) {
      throw CorruptionException("Failed to deserialize stored auth data", e)
    }
  }

  override suspend fun writeTo(t: AuthData, output: OutputStream) {
    val jsonBytes = Json.encodeToString(t).encodeToByteArray()
    val encryptedBytes = aead.encrypt(jsonBytes, ASSOCIATED_DATA)
    output.write(encryptedBytes)
  }

  companion object {
    private val ASSOCIATED_DATA = "pinosu_auth_data".toByteArray()
  }
}
