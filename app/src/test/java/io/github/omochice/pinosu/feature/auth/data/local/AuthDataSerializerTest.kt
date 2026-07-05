package io.github.omochice.pinosu.feature.auth.data.local

import androidx.datastore.core.CorruptionException
import com.google.crypto.tink.Aead
import io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.GeneralSecurityException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [AuthDataSerializer]
 *
 * Tests encryption/decryption logic using mocked Aead, verifying that data is properly
 * encrypted/decrypted and edge cases are handled correctly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthDataSerializerTest {

  private lateinit var mockAead: Aead
  private lateinit var serializer: AuthDataSerializer

  @BeforeTest
  fun setup() {
    mockAead = mockk()
    serializer = AuthDataSerializer(mockAead)
  }

  @Test
  fun `readFrom with empty input returns default value`() = runTest {
    val emptyInput = ByteArrayInputStream(ByteArray(0))

    val result = serializer.readFrom(emptyInput)

    assertEquals(AuthData.DEFAULT, result)
  }

  @Test
  fun `readFrom with valid encrypted data returns decrypted AuthData`() = runTest {
    val authData = AuthData(userPubkey = "abc123", createdAt = 1000L, lastAccessed = 2000L)
    val jsonBytes = Json.encodeToString(authData).encodeToByteArray()
    val encryptedBytes = "encrypted_data".toByteArray()
    val associatedData = "pinosu_auth_data".toByteArray()

    every { mockAead.decrypt(encryptedBytes, associatedData) } returns jsonBytes

    val input = ByteArrayInputStream(encryptedBytes)
    val result = serializer.readFrom(input)

    assertEquals(authData, result)
  }

  @Test
  fun `readFrom when decrypt fails throws CorruptionException instead of default`() = runTest {
    val encryptedBytes = "corrupted_data".toByteArray()
    val associatedData = "pinosu_auth_data".toByteArray()

    every { mockAead.decrypt(encryptedBytes, associatedData) } throws
        GeneralSecurityException("Decryption failed")

    assertFailsWith<CorruptionException> {
      serializer.readFrom(ByteArrayInputStream(encryptedBytes))
    }
  }

  @Test
  fun `readFrom when deserialization fails throws CorruptionException instead of default`() =
      runTest {
        val encryptedBytes = "encrypted_data".toByteArray()
        val associatedData = "pinosu_auth_data".toByteArray()

        every { mockAead.decrypt(encryptedBytes, associatedData) } returns
            "not-valid-json".toByteArray()

        assertFailsWith<CorruptionException> {
          serializer.readFrom(ByteArrayInputStream(encryptedBytes))
        }
      }

  @Test
  fun `writeTo encrypts data and writes to output stream`() = runTest {
    val authData = AuthData(userPubkey = "test_pubkey", createdAt = 5000L, lastAccessed = 6000L)
    val jsonBytes = Json.encodeToString(authData).encodeToByteArray()
    val encryptedBytes = "encrypted_output".toByteArray()
    val associatedData = "pinosu_auth_data".toByteArray()

    every { mockAead.encrypt(jsonBytes, associatedData) } returns encryptedBytes

    val output = ByteArrayOutputStream()
    serializer.writeTo(authData, output)

    verify { mockAead.encrypt(jsonBytes, associatedData) }
    assertContentEquals(encryptedBytes, output.toByteArray())
  }

  @Test
  fun `AuthData with ReadOnly loginMode should serialize and deserialize correctly`() = runTest {
    val authData = AuthData(userPubkey = "npub1test", loginMode = LoginMode.ReadOnly)

    val json = Json.encodeToString(authData)
    val decoded = Json.decodeFromString<AuthData>(json)

    assertEquals(LoginMode.ReadOnly, decoded.loginMode)
  }

  @Test
  fun `AuthData default loginMode should be Nip55Signer`() {
    val authData = AuthData()

    assertEquals(LoginMode.Nip55Signer, authData.loginMode)
  }
}
