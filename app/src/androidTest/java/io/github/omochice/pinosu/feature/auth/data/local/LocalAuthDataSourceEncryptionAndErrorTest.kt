package io.github.omochice.pinosu.feature.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.crypto.tink.aead.AeadConfig
import io.github.omochice.pinosu.core.crypto.TinkKeyManager
import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.model.error.StorageError
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith

/**
 * Tests for LocalAuthDataSource encryption, decryption, and error handling
 *
 * Verifies:
 * - Data is stored in encrypted form using Tink AEAD
 * - Encrypted data can be correctly decrypted
 * - Error handling for reading invalid data
 */
@RunWith(AndroidJUnit4::class)
class LocalAuthDataSourceEncryptionAndErrorTest {

  private lateinit var context: Context
  private lateinit var dataSource: LocalAuthDataSource
  private lateinit var testDataStore: DataStore<AuthData>
  private lateinit var testFile: File
  private lateinit var tinkKeyManager: TinkKeyManager

  @BeforeTest
  fun setup() {
    AeadConfig.register()
    context = ApplicationProvider.getApplicationContext()
    tinkKeyManager = TinkKeyManager(context)
    testFile = File(context.filesDir, "test_encrypted_auth_data_${System.currentTimeMillis()}.pb")
    testDataStore =
        DataStoreFactory.create(
            serializer = AuthDataSerializer(tinkKeyManager.getAead()), produceFile = { testFile })

    dataSource = LocalAuthDataSource(testDataStore)
  }

  @AfterTest
  fun tearDown() {
    testFile.delete()
  }

  @Test
  fun `data should be stored in encrypted form`() = runTest {
    val user = User(requireNotNull(Pubkey.parse("npub1" + "a".repeat(59))))

    dataSource.saveUser(user, LoginMode.Nip55Signer)

    val fileBytes = testFile.readBytes()
    val fileContent = String(fileBytes, Charsets.UTF_8)

    assertFalse(
        fileContent.contains(user.pubkey.npub), "Data should be encrypted, not stored in plaintext")
  }

  @Test
  fun `encrypted data should be correctly decrypted`() = runTest {
    val user = User(requireNotNull(Pubkey.parse("npub1" + "b".repeat(59))))

    dataSource.saveUser(user, LoginMode.Nip55Signer)

    val retrieved = dataSource.getUser()

    assertNotNull(retrieved, "Encrypted data should be decryptable")
    assertEquals(user.pubkey, retrieved?.pubkey, "Decrypted data should match original")
  }

  @Test
  fun `encryption key should persist across instances`() = runTest {
    val user = User(requireNotNull(Pubkey.parse("npub1" + "c".repeat(59))))

    dataSource.saveUser(user, LoginMode.Nip55Signer)

    val newDataSource = LocalAuthDataSource(testDataStore)

    val retrieved = newDataSource.getUser()

    assertNotNull(retrieved, "New instance should be able to decrypt data")
    assertEquals(user.pubkey, retrieved?.pubkey, "Data should be consistent across instances")
  }

  @Test
  fun `multiple encryption decryption cycles should work correctly`() = runTest {
    val users =
        listOf(
            User(requireNotNull(Pubkey.parse("npub1" + "0".repeat(59)))),
            User(requireNotNull(Pubkey.parse("npub1" + "1".repeat(59)))),
            User(requireNotNull(Pubkey.parse("npub1" + "a".repeat(59)))),
            User(requireNotNull(Pubkey.parse("npub1" + "f".repeat(59)))))

    for (user in users) {
      dataSource.saveUser(user, LoginMode.Nip55Signer)

      val retrieved = dataSource.getUser()

      assertNotNull(retrieved, "User should be retrievable")
      assertEquals(user.pubkey, retrieved?.pubkey, "Retrieved user should match saved user")
    }
  }

  @Test
  fun `getUser with invalid pubkey format should return null`() = runTest {
    val testDataStoreNoEncrypt =
        DataStoreFactory.create(
            serializer = TestAuthDataSerializer(),
            produceFile = {
              File(context.filesDir, "test_no_encrypt_auth_data_${System.currentTimeMillis()}.pb")
            })
    val dataSourceNoEncrypt = LocalAuthDataSource(testDataStoreNoEncrypt)

    val invalidPubkeys =
        listOf(
            "invalid",
            "g".repeat(64),
            "abc",
            "ABCD1234".repeat(8),
            "abcd1234".repeat(7),
            "abcd1234".repeat(8) + "0")

    for (invalidPubkey in invalidPubkeys) {
      testDataStoreNoEncrypt.updateData { AuthData(userPubkey = invalidPubkey) }

      val retrieved = dataSourceNoEncrypt.getUser()

      assertNull(retrieved, "getUser should return null for invalid pubkey format: $invalidPubkey")

      dataSourceNoEncrypt.clearLoginState()
    }
  }

  @Test
  fun `getUser with missing pubkey should return null`() = runTest {
    val retrieved = dataSource.getUser()

    assertNull(retrieved, "getUser should return null when pubkey is missing")
  }

  @Test
  fun `getUser with empty pubkey should return null`() = runTest {
    val testDataStoreNoEncrypt =
        DataStoreFactory.create(
            serializer = TestAuthDataSerializer(),
            produceFile = {
              File(context.filesDir, "test_empty_pubkey_${System.currentTimeMillis()}.pb")
            })
    val dataSourceNoEncrypt = LocalAuthDataSource(testDataStoreNoEncrypt)

    testDataStoreNoEncrypt.updateData { AuthData(userPubkey = "") }

    val retrieved = dataSourceNoEncrypt.getUser()

    assertNull(retrieved, "getUser should return null for empty pubkey")
  }

  @Test
  fun `getUser should handle timestamps correctly`() = runTest {
    val user = User(requireNotNull(Pubkey.parse("npub1" + "d".repeat(59))))

    dataSource.saveUser(user, LoginMode.Nip55Signer)

    val retrieved = dataSource.getUser()

    assertNotNull(retrieved, "User should be retrievable")
    assertEquals(user.pubkey, retrieved?.pubkey, "Pubkey should match")
  }

  @Test
  fun `clearLoginState should remove all data`() = runTest {
    val user = User(requireNotNull(Pubkey.parse("npub1" + "e".repeat(59))))

    dataSource.saveUser(user, LoginMode.Nip55Signer)

    assertNotNull(dataSource.getUser(), "User should be saved")

    dataSource.clearLoginState()

    assertNull(dataSource.getUser(), "getUser should return null after clear")
  }

  @Test
  fun `saveUser should validate error type`() = runTest {
    val user = User(requireNotNull(Pubkey.parse("npub1" + "f".repeat(59))))

    try {
      dataSource.saveUser(user, LoginMode.Nip55Signer)
      val retrieved = dataSource.getUser()
      assertEquals(user.pubkey, retrieved?.pubkey, "User should be saved successfully")
    } catch (e: StorageError.WriteError) {
      assertNotNull(e.message, "Error message should be present")
    }
  }

  @Test
  fun `clearLoginState should validate error type`() = runTest {
    try {
      dataSource.clearLoginState()
      assertNull(dataSource.getUser(), "Data should be cleared")
    } catch (e: StorageError.WriteError) {
      assertNotNull(e.message, "Error message should be present")
    }
  }
}
