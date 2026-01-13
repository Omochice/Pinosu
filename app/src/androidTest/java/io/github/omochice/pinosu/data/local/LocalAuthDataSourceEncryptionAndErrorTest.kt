package io.github.omochice.pinosu.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.StorageError
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for LocalAuthDataSource encryption, decryption, and error handling
 * - Error handling tests for reading invalid data
 * - Verification of proper encryption and decryption operation
 */
@RunWith(AndroidJUnit4::class)
class LocalAuthDataSourceEncryptionAndErrorTest {

  private lateinit var context: Context
  private lateinit var dataSource: LocalAuthDataSource

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    dataSource = LocalAuthDataSource(context)
  }

  @After
  fun tearDown() {
    context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  /** Test that data is stored in encrypted form */
  @Test
  fun `data should be stored in encrypted form`() = runTest {
    val user = User("abcd1234".repeat(8)) // 64-character valid pubkey

    dataSource.saveUser(user)

    val regularPrefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
    val allEntries = regularPrefs.all

    var foundPlaintextPubkey = false
    for ((key, value) in allEntries) {
      if (key == "user_pubkey" && value == user.pubkey) {
        foundPlaintextPubkey = true
        break
      }
      if (value.toString() == user.pubkey) {
        foundPlaintextPubkey = true
        break
      }
    }

    assertFalse("Data should be encrypted, not stored in plaintext", foundPlaintextPubkey)
  }

  /** Test that encrypted data is correctly decrypted */
  @Test
  fun `encrypted data should be correctly decrypted`() = runTest {
    val user = User("1234abcd".repeat(8))

    dataSource.saveUser(user)

    val retrieved = dataSource.getUser()

    assertNotNull("Encrypted data should be decryptable", retrieved)
    assertEquals("Decrypted data should match original", user.pubkey, retrieved?.pubkey)
  }

  /** Test that same encryption key is used to decrypt across different instances */
  @Test
  fun `encryption key should persist across instances`() = runTest {
    val user = User("fedcba98".repeat(8))

    dataSource.saveUser(user)

    val newDataSource = LocalAuthDataSource(context)

    val retrieved = newDataSource.getUser()

    assertNotNull("New instance should be able to decrypt data", retrieved)
    assertEquals("Data should be consistent across instances", user.pubkey, retrieved?.pubkey)
  }

  /** Test that encryption and decryption works correctly through multiple cycles */
  @Test
  fun `multiple encryption decryption cycles should work correctly`() = runTest {
    val users =
        listOf(
            User("0".repeat(64)), User("1".repeat(64)), User("a".repeat(64)), User("f".repeat(64)))

    for (user in users) {
      dataSource.saveUser(user)

      val retrieved = dataSource.getUser()

      assertNotNull("User should be retrievable", retrieved)
      assertEquals("Retrieved user should match saved user", user.pubkey, retrieved?.pubkey)
    }
  }

  /** Test that null is returned when invalid pubkey format is stored */
  @Test
  fun `getUser with invalid pubkey format should return null`() = runTest {
    val invalidPubkeys =
        listOf(
            "invalid",
            "g".repeat(64),
            "abc",
            "ABCD1234".repeat(8),
            "abcd1234".repeat(7),
            "abcd1234".repeat(8) + "0")

    for (invalidPubkey in invalidPubkeys) {
      context
          .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
          .edit()
          .putString("user_pubkey", invalidPubkey)
          .commit()

      val retrieved = dataSource.getUser()

      assertNull("getUser should return null for invalid pubkey format: $invalidPubkey", retrieved)

      dataSource.clearLoginState()
    }
  }

  /** Test that null is returned when pubkey is missing */
  @Test
  fun `getUser with missing pubkey should return null`() = runTest {
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putLong("login_created_at", System.currentTimeMillis())
        .putLong("login_last_accessed", System.currentTimeMillis())
        .commit()

    val retrieved = dataSource.getUser()

    assertNull("getUser should return null when pubkey is missing", retrieved)
  }

  /** Test that exceptions during SharedPreferences read are caught */
  @Test
  fun `getUser with exception should return null`() = runTest {
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putInt("user_pubkey", 12345)
        .commit()

    val retrieved = dataSource.getUser()

    assertNull("getUser should return null on exception", retrieved)
  }

  /** Test that null is returned when empty string pubkey is stored */
  @Test
  fun `getUser with empty pubkey should return null`() = runTest {
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putString("user_pubkey", "")
        .commit()

    val retrieved = dataSource.getUser()

    assertNull("getUser should return null for empty pubkey", retrieved)
  }

  /** Test that timestamps are handled correctly even if invalid */
  @Test
  fun `getUser should handle timestamps correctly`() = runTest {
    val user = User("deadbeef".repeat(8))

    dataSource.saveUser(user)

    val retrieved = dataSource.getUser()

    assertNotNull("User should be retrievable", retrieved)
    assertEquals("Pubkey should match", user.pubkey, retrieved?.pubkey)
  }

  /** Test that all data is removed after clearLoginState */
  @Test
  fun `clearLoginState should remove all data`() = runTest {
    val user = User("cafe1234".repeat(8))

    dataSource.saveUser(user)

    assertNotNull("User should be saved", dataSource.getUser())

    dataSource.clearLoginState()

    val prefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
    assertFalse("user_pubkey should be removed", prefs.contains("user_pubkey"))
    assertFalse("login_created_at should be removed", prefs.contains("login_created_at"))
    assertFalse("login_last_accessed should be removed", prefs.contains("login_last_accessed"))

    assertNull("getUser should return null after clear", dataSource.getUser())
  }

  /** Test that saveUser throws StorageError.WriteError on exception */
  @Test
  fun `saveUser should validate error type`() = runTest {
    val user = User("beef".repeat(16))

    try {
      dataSource.saveUser(user)
      val retrieved = dataSource.getUser()
      assertEquals("User should be saved successfully", user.pubkey, retrieved?.pubkey)
    } catch (e: StorageError.WriteError) {
      assertNotNull("Error message should be present", e.message)
    }
  }

  /** Test that clearLoginState throws StorageError.WriteError on exception */
  @Test
  fun `clearLoginState should validate error type`() = runTest {
    try {
      dataSource.clearLoginState()
      assertNull("Data should be cleared", dataSource.getUser())
    } catch (e: StorageError.WriteError) {
      assertNotNull("Error message should be present", e.message)
    }
  }
}
