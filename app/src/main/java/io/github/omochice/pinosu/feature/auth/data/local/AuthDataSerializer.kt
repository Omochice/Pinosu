package io.github.omochice.pinosu.feature.auth.data.local

import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import java.io.InputStream
import java.io.OutputStream
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

    return try {
      val decryptedBytes = aead.decrypt(encryptedBytes, ASSOCIATED_DATA)
      Json.decodeFromString<AuthData>(decryptedBytes.decodeToString())
    } catch (_: Exception) {
      defaultValue
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
