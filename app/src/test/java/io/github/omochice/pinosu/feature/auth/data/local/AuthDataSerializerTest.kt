package io.github.omochice.pinosu.feature.auth.data.local

import com.google.crypto.tink.Aead
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.GeneralSecurityException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
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

  @Before
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
  fun `readFrom when decrypt throws exception returns default value`() = runTest {
    val encryptedBytes = "corrupted_data".toByteArray()
    val associatedData = "pinosu_auth_data".toByteArray()

    every { mockAead.decrypt(encryptedBytes, associatedData) } throws
        GeneralSecurityException("Decryption failed")

    val input = ByteArrayInputStream(encryptedBytes)
    val result = serializer.readFrom(input)

    assertEquals(AuthData.DEFAULT, result)
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
    assertArrayEquals(encryptedBytes, output.toByteArray())
  }
}
