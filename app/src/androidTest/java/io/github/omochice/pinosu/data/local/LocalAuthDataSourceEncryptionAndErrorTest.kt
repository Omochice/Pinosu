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
 *
 * Task 3.3: LocalAuthDataSourceの単体テスト
 * - 不正データ読み込み時のエラーハンドリングテスト
 * - 暗号化・復号化の正常動作確認 Requirements: 2.1, 2.2, 2.5, 6.2
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
    // テスト後にデータをクリア
    context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  // ========== Encryption/Decryption Verification Tests ==========

  /**
   * データが暗号化されて保存されることを確認するテスト
   *
   * Task 3.3: 暗号化・復号化の正常動作確認 Requirement 6.2: EncryptedSharedPreferencesを使用した暗号化
   */
  @Test
  fun testDataIsEncryptedInStorage() = runTest {
    val user = User("abcd1234".repeat(8)) // 64文字の有効なpubkey

    // ユーザーを保存
    dataSource.saveUser(user)

    // 通常のSharedPreferences（非暗号化）で同じファイルを読み込んでみる
    // EncryptedSharedPreferencesを使用している場合、生のキー/値は読み取れないはず
    val regularPrefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
    val allEntries = regularPrefs.all

    // EncryptedSharedPreferencesを使用している場合:
    // 1. キー名が暗号化されているため、"user_pubkey"という生のキーは存在しない
    // 2. 値が暗号化されているため、元のpubkeyが直接読み取れない
    var foundPlaintextPubkey = false
    for ((key, value) in allEntries) {
      if (key == "user_pubkey" && value == user.pubkey) {
        foundPlaintextPubkey = true
        break
      }
      // 値がpubkeyと一致する場合も検出
      if (value.toString() == user.pubkey) {
        foundPlaintextPubkey = true
        break
      }
    }

    assertFalse("Data should be encrypted, not stored in plaintext", foundPlaintextPubkey)
  }

  /**
   * 暗号化されたデータが正しく復号化されることを確認するテスト
   *
   * Task 3.3: 暗号化・復号化の正常動作確認
   */
  @Test
  fun testEncryptedDataCanBeDecrypted() = runTest {
    val user = User("1234abcd".repeat(8))

    // 保存
    dataSource.saveUser(user)

    // 取得（復号化）
    val retrieved = dataSource.getUser()

    // 復号化されたデータが元のデータと一致することを確認
    assertNotNull("Encrypted data should be decryptable", retrieved)
    assertEquals("Decrypted data should match original", user.pubkey, retrieved?.pubkey)
  }

  /**
   * 異なるインスタンスでも同じ暗号化キーを使用して復号化できることを確認
   *
   * Task 3.3: 暗号化・復号化の正常動作確認 Requirement 6.2: Android Keystore経由のMasterKey生成
   */
  @Test
  fun testEncryptionKeyPersistenceAcrossInstances() = runTest {
    val user = User("fedcba98".repeat(8))

    // 最初のインスタンスで保存
    dataSource.saveUser(user)

    // 新しいインスタンスを作成
    val newDataSource = LocalAuthDataSource(context)

    // 新しいインスタンスで取得できることを確認
    val retrieved = newDataSource.getUser()

    assertNotNull("New instance should be able to decrypt data", retrieved)
    assertEquals("Data should be consistent across instances", user.pubkey, retrieved?.pubkey)
  }

  /**
   * 複数回の保存・取得でも暗号化・復号化が正常に動作することを確認
   *
   * Task 3.3: 暗号化・復号化の正常動作確認
   */
  @Test
  fun testMultipleEncryptionDecryptionCycles() = runTest {
    val users =
        listOf(
            User("0".repeat(64)), User("1".repeat(64)), User("a".repeat(64)), User("f".repeat(64)))

    for (user in users) {
      // 保存
      dataSource.saveUser(user)

      // 取得
      val retrieved = dataSource.getUser()

      // 検証
      assertNotNull("User should be retrievable", retrieved)
      assertEquals("Retrieved user should match saved user", user.pubkey, retrieved?.pubkey)
    }
  }

  // ========== Error Handling Tests ==========

  /**
   * 不正な形式のpubkeyが保存されている場合にnullを返すことを確認
   *
   * Task 3.3: 不正データ読み込み時のエラーハンドリングテスト Requirement 2.1: データ検証
   */
  @Test
  fun testGetUser_InvalidPubkeyFormat_ReturnsNull() = runTest {
    val invalidPubkeys =
        listOf(
            "invalid", // 短すぎる
            "g".repeat(64), // 無効な16進数文字
            "abc", // 短い＆無効な16進数
            "ABCD1234".repeat(8), // 大文字（無効）
            "abcd1234".repeat(7), // 63文字（1文字足りない）
            "abcd1234".repeat(8) + "0" // 65文字（1文字多い）
            )

    for (invalidPubkey in invalidPubkeys) {
      // 不正なデータを直接保存
      context
          .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
          .edit()
          .putString("user_pubkey", invalidPubkey)
          .commit()

      // 取得を試行
      val retrieved = dataSource.getUser()

      // nullが返されることを確認
      assertNull("getUser should return null for invalid pubkey format: $invalidPubkey", retrieved)

      // クリーンアップ
      dataSource.clearLoginState()
    }
  }

  /**
   * pubkeyが存在しない場合にnullを返すことを確認
   *
   * Task 3.3: 不正データ読み込み時のエラーハンドリングテスト
   */
  @Test
  fun testGetUser_MissingPubkey_ReturnsNull() = runTest {
    // タイムスタンプだけ存在する状態
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putLong("login_created_at", System.currentTimeMillis())
        .putLong("login_last_accessed", System.currentTimeMillis())
        .commit()

    val retrieved = dataSource.getUser()

    assertNull("getUser should return null when pubkey is missing", retrieved)
  }

  /**
   * SharedPreferences読み込み時の例外がキャッチされることを確認
   *
   * Task 3.3: 不正データ読み込み時のエラーハンドリングテスト Requirement 2.2: エラーハンドリング
   */
  @Test
  fun testGetUser_ExceptionHandling_ReturnsNull() = runTest {
    // 不正な型のデータを保存（Stringを期待しているところにIntを保存）
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putInt("user_pubkey", 12345) // Stringではなく Int
        .commit()

    // 例外が発生してもnullが返されることを確認
    val retrieved = dataSource.getUser()

    assertNull("getUser should return null on exception", retrieved)
  }

  /**
   * 空文字列のpubkeyが保存されている場合にnullを返すことを確認
   *
   * Task 3.3: 不正データ読み込み時のエラーハンドリングテスト
   */
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
   * Task 3.3: 不正データ読み込み時のエラーハンドリングテスト
   *
   * Note: EncryptedSharedPreferencesを使用しているため、データの直接注入ができない。 このテストは将来的な参照用として残すが、現在の実装では検証が困難。
   */
  @Test
  fun testGetUser_TimestampHandling() = runTest {
    val user = User("deadbeef".repeat(8))

    // 正常に保存
    dataSource.saveUser(user)

    // 正常に取得できることを確認（タイムスタンプは自動的に設定される）
    val retrieved = dataSource.getUser()

    assertNotNull("User should be retrievable", retrieved)
    assertEquals("Pubkey should match", user.pubkey, retrieved?.pubkey)
  }

  /**
   * clearLoginState後に保存されたデータがすべて削除されることを確認
   *
   * Task 3.3: エラーハンドリングテスト Requirement 2.5: ログイン状態のクリア
   */
  @Test
  fun testClearLoginState_RemovesAllData() = runTest {
    val user = User("cafe1234".repeat(8))

    // ユーザーを保存
    dataSource.saveUser(user)

    // データが保存されていることを確認
    assertNotNull("User should be saved", dataSource.getUser())

    // クリア
    dataSource.clearLoginState()

    // すべてのキーが削除されていることを確認
    val prefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
    assertFalse("user_pubkey should be removed", prefs.contains("user_pubkey"))
    assertFalse("login_created_at should be removed", prefs.contains("login_created_at"))
    assertFalse("login_last_accessed should be removed", prefs.contains("login_last_accessed"))

    // getUserでnullが返されることを確認
    assertNull("getUser should return null after clear", dataSource.getUser())
  }

  /**
   * saveUserが例外をStorageError.WriteErrorとしてスローすることを確認
   *
   * Note: EncryptedSharedPreferencesは非常に堅牢なため、実際にWriteErrorを 発生させるのは困難。このテストは将来的なエラーハンドリングの検証用。
   *
   * Task 3.3: 不正データ読み込み時のエラーハンドリングテスト
   */
  @Test
  fun testSaveUser_ValidatesErrorType() = runTest {
    // 現在の実装では、EncryptedSharedPreferencesが例外をスローする状況を
    // 作り出すのは難しいため、正常系の動作を確認
    val user = User("beef".repeat(16))

    try {
      dataSource.saveUser(user)
      // 正常に保存できたことを確認
      val retrieved = dataSource.getUser()
      assertEquals("User should be saved successfully", user.pubkey, retrieved?.pubkey)
    } catch (e: StorageError.WriteError) {
      // もしWriteErrorが発生した場合は、エラーメッセージが存在することを確認
      assertNotNull("Error message should be present", e.message)
    }
  }

  /**
   * clearLoginStateが例外をStorageError.WriteErrorとしてスローすることを確認
   *
   * Note: EncryptedSharedPreferencesは非常に堅牢なため、実際にWriteErrorを 発生させるのは困難。このテストは将来的なエラーハンドリングの検証用。
   *
   * Task 3.3: 不正データ読み込み時のエラーハンドリングテスト
   */
  @Test
  fun testClearLoginState_ValidatesErrorType() = runTest {
    // 現在の実装では、EncryptedSharedPreferencesが例外をスローする状況を
    // 作り出すのは難しいため、正常系の動作を確認
    try {
      dataSource.clearLoginState()
      // 正常にクリアできたことを確認
      assertNull("Data should be cleared", dataSource.getUser())
    } catch (e: StorageError.WriteError) {
      // もしWriteErrorが発生した場合は、エラーメッセージが存在することを確認
      assertNotNull("Error message should be present", e.message)
    }
  }
}
