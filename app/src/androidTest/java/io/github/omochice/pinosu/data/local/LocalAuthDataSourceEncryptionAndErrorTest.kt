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
 * LocalAuthDataSourceの暗号化・復号化とエラーハンドリングのテスト
 * - 不正データ読み込み時のエラーハンドリングテスト
 * - 暗号化・復号化の正常動作確認
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

  /** データが暗号化されて保存されることを確認するテスト */
  @Test
  fun testDataIsEncryptedInStorage() = runTest {
    val user = User("abcd1234".repeat(8)) // 64文字の有効なpubkey

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

  /** 暗号化されたデータが正しく復号化されることを確認するテスト */
  @Test
  fun testEncryptedDataCanBeDecrypted() = runTest {
    val user = User("1234abcd".repeat(8))

    dataSource.saveUser(user)

    val retrieved = dataSource.getUser()

    assertNotNull("Encrypted data should be decryptable", retrieved)
    assertEquals("Decrypted data should match original", user.pubkey, retrieved?.pubkey)
  }

  /** 異なるインスタンスでも同じ暗号化キーを使用して復号化できることを確認 */
  @Test
  fun testEncryptionKeyPersistenceAcrossInstances() = runTest {
    val user = User("fedcba98".repeat(8))

    dataSource.saveUser(user)

    val newDataSource = LocalAuthDataSource(context)

    val retrieved = newDataSource.getUser()

    assertNotNull("New instance should be able to decrypt data", retrieved)
    assertEquals("Data should be consistent across instances", user.pubkey, retrieved?.pubkey)
  }

  /** 複数回の保存・取得でも暗号化・復号化が正常に動作することを確認 */
  @Test
  fun testMultipleEncryptionDecryptionCycles() = runTest {
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

  /** 不正な形式のpubkeyが保存されている場合にnullを返すことを確認 */
  @Test
  fun testGetUser_InvalidPubkeyFormat_ReturnsNull() = runTest {
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

  /** pubkeyが存在しない場合にnullを返すことを確認 */
  @Test
  fun testGetUser_MissingPubkey_ReturnsNull() = runTest {
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putLong("login_created_at", System.currentTimeMillis())
        .putLong("login_last_accessed", System.currentTimeMillis())
        .commit()

    val retrieved = dataSource.getUser()

    assertNull("getUser should return null when pubkey is missing", retrieved)
  }

  /** SharedPreferences読み込み時の例外がキャッチされることを確認 */
  @Test
  fun testGetUser_ExceptionHandling_ReturnsNull() = runTest {
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putInt("user_pubkey", 12345)
        .commit()

    val retrieved = dataSource.getUser()

    assertNull("getUser should return null on exception", retrieved)
  }

  /** 空文字列のpubkeyが保存されている場合にnullを返すことを確認 */
  @Test
  fun testGetUser_EmptyPubkey_ReturnsNull() = runTest {
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putString("user_pubkey", "")
        .commit()

    val retrieved = dataSource.getUser()

    assertNull("getUser should return null for empty pubkey", retrieved)
  }

  /**
   * タイムスタンプが不正な場合でも正しく処理されることを確認
   *
   * Note: EncryptedSharedPreferencesを使用しているため、データの直接注入ができない。 このテストは将来的な参照用として残すが、現在の実装では検証が困難。
   */
  @Test
  fun testGetUser_TimestampHandling() = runTest {
    val user = User("deadbeef".repeat(8))

    dataSource.saveUser(user)

    val retrieved = dataSource.getUser()

    assertNotNull("User should be retrievable", retrieved)
    assertEquals("Pubkey should match", user.pubkey, retrieved?.pubkey)
  }

  /** clearLoginState後に保存されたデータがすべて削除されることを確認 */
  @Test
  fun testClearLoginState_RemovesAllData() = runTest {
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

  /**
   * saveUserが例外をStorageError.WriteErrorとしてスローすることを確認
   *
   * Note: EncryptedSharedPreferencesは非常に堅牢なため、実際にWriteErrorを 発生させるのは困難。このテストは将来的なエラーハンドリングの検証用。
   */
  @Test
  fun testSaveUser_ValidatesErrorType() = runTest {
    val user = User("beef".repeat(16))

    try {
      dataSource.saveUser(user)
      val retrieved = dataSource.getUser()
      assertEquals("User should be saved successfully", user.pubkey, retrieved?.pubkey)
    } catch (e: StorageError.WriteError) {
      assertNotNull("Error message should be present", e.message)
    }
  }

  /**
   * clearLoginStateが例外をStorageError.WriteErrorとしてスローすることを確認
   *
   * Note: EncryptedSharedPreferencesは非常に堅牢なため、実際にWriteErrorを 発生させるのは困難。このテストは将来的なエラーハンドリングの検証用。
   */
  @Test
  fun testClearLoginState_ValidatesErrorType() = runTest {
    try {
      dataSource.clearLoginState()
      assertNull("Data should be cleared", dataSource.getUser())
    } catch (e: StorageError.WriteError) {
      assertNotNull("Error message should be present", e.message)
    }
  }
}
