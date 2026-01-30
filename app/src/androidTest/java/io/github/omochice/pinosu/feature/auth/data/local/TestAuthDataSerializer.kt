package io.github.omochice.pinosu.feature.auth.data.local

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.json.Json

/**
 * Non-encrypted DataStore Serializer for testing
 *
 * Uses plain JSON serialization without encryption for test isolation and performance.
 */
class TestAuthDataSerializer : Serializer<AuthData> {

  override val defaultValue: AuthData = AuthData.DEFAULT

  override suspend fun readFrom(input: InputStream): AuthData {
    val bytes = input.readBytes()
    if (bytes.isEmpty()) {
      return defaultValue
    }

    return try {
      Json.decodeFromString<AuthData>(bytes.decodeToString())
    } catch (e: Exception) {
      defaultValue
    }
  }

  override suspend fun writeTo(t: AuthData, output: OutputStream) {
    val jsonBytes = Json.encodeToString(t).encodeToByteArray()
    output.write(jsonBytes)
  }
}
