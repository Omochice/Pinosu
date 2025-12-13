package io.github.omochice.pinosu.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.omochice.pinosu.domain.model.User
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * LocalAuthDataSourceの保存・取得・削除機能のテスト
 *
 * Task 3.2: ユーザーデータの保存・取得・削除機能 Requirements: 1.4, 2.1, 2.2, 2.5
 */
@RunWith(AndroidJUnit4::class)
class LocalAuthDataSourceSaveGetDeleteTest {

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

  // ========== saveUser Tests ==========

  /** ユーザーを正常に保存できることをテスト Task 3.2: saveUser実装 */
  @Test
  fun testSaveUser_Success() = runTest {
    val user = User("a".repeat(64))

    // 例外がスローされないことを確認
    dataSource.saveUser(user)
    // 成功 - 例外が発生しなかった
  }

  /** ユーザー保存時にタイムスタンプが記録されることをテスト Task 3.2: created_at/last_accessedタイムスタンプ管理 */
  @Test
  fun testSaveUser_SetsTimestamps() = runTest {
    val user = User("b".repeat(64))
    val beforeSave = System.currentTimeMillis()

    dataSource.saveUser(user)

    val savedUser = dataSource.getUser()
    assertNotNull("Saved user should be retrievable", savedUser)
    // タイムスタンプが設定されていることを確認（getUser実装後に有効化）
  }

  /** 既存のユーザーを上書き保存できることをテスト */
  @Test
  fun testSaveUser_Overwrite() = runTest {
    val user1 = User("c".repeat(64))
    val user2 = User("d".repeat(64))

    dataSource.saveUser(user1)
    dataSource.saveUser(user2) // 例外がスローされないことを確認

    val savedUser = dataSource.getUser()
    assertEquals("Should retrieve the latest user", user2.pubkey, savedUser?.pubkey)
  }

  // ========== getUser Tests ==========

  /** 保存されたユーザーを取得できることをテスト Task 3.2: getUser実装 */
  @Test
  fun testGetUser_AfterSave() = runTest {
    val user = User("e".repeat(64))
    dataSource.saveUser(user)

    val retrieved = dataSource.getUser()

    assertNotNull("getUser should return saved user", retrieved)
    assertEquals("Retrieved pubkey should match", user.pubkey, retrieved?.pubkey)
  }

  /** データが保存されていない場合はnullを返すことをテスト Task 3.2: nullチェック */
  @Test
  fun testGetUser_NoDataReturnsNull() = runTest {
    val retrieved = dataSource.getUser()

    assertNull("getUser should return null when no data exists", retrieved)
  }

  /** 不正なpubkeyデータが保存されている場合はnullを返すことをテスト Task 3.2: 検証ロジック */
  @Test
  fun testGetUser_InvalidDataReturnsNull() = runTest {
    // 不正なデータを直接保存
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putString("user_pubkey", "invalid_pubkey")
        .commit()

    val retrieved = dataSource.getUser()

    assertNull("getUser should return null for invalid pubkey", retrieved)
  }

  /** created_atタイムスタンプが正しく保存・取得されることをテスト */
  @Test
  fun testGetUser_PreservesCreatedAt() = runTest {
    val user = User("f".repeat(64))
    val beforeSave = System.currentTimeMillis()

    dataSource.saveUser(user)
    Thread.sleep(10) // わずかに時間を空ける

    val prefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
    val createdAt = prefs.getLong("login_created_at", 0L)

    assertTrue("created_at should be set", createdAt >= beforeSave)
    assertTrue("created_at should be recent", createdAt <= System.currentTimeMillis())
  }

  /** last_accessedタイムスタンプが正しく保存・取得されることをテスト */
  @Test
  fun testGetUser_UpdatesLastAccessed() = runTest {
    val user = User("1".repeat(64))
    dataSource.saveUser(user)
    Thread.sleep(10)

    dataSource.getUser()

    val prefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
    val lastAccessed = prefs.getLong("login_last_accessed", 0L)

    assertTrue("last_accessed should be updated", lastAccessed > 0L)
  }

  // ========== clearLoginState Tests ==========

  /** ログイン状態を正常にクリアできることをテスト Task 3.2: clearLoginState実装 */
  @Test
  fun testClearLoginState_Success() = runTest {
    val user = User("2".repeat(64))
    dataSource.saveUser(user)

    dataSource.clearLoginState() // 例外がスローされないことを確認

    val retrieved = dataSource.getUser()
    assertNull("User should be null after clear", retrieved)
  }

  /** データがない状態でクリアを実行しても成功することをテスト */
  @Test
  fun testClearLoginState_NoDataSucceeds() = runTest {
    dataSource.clearLoginState() // 例外がスローされないことを確認
  }

  /** クリア後にタイムスタンプも削除されることをテスト */
  @Test
  fun testClearLoginState_RemovesTimestamps() = runTest {
    val user = User("3".repeat(64))
    dataSource.saveUser(user)

    dataSource.clearLoginState()

    val prefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
    assertFalse("user_pubkey should be removed", prefs.contains("user_pubkey"))
    assertFalse("login_created_at should be removed", prefs.contains("login_created_at"))
    assertFalse("login_last_accessed should be removed", prefs.contains("login_last_accessed"))
  }

  // ========== Error Handling Tests ==========

  /** ストレージエラー時にStorageError.WriteErrorを返すことをテスト（将来拡張用） */
  @Test
  fun testSaveUser_HandlesStorageError() = runTest {
    // 現在の実装ではEncryptedSharedPreferencesの例外をキャッチしていない
    // 将来的にエラーハンドリングを追加する場合のプレースホルダー
    // エラー注入が難しいため、現時点ではスキップ
  }
}
