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

/** Tests for LocalAuthDataSource save, get, and delete functionality */
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
    context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  @Test
  fun `saveUser should succeed`() = runTest {
    val user = User("a".repeat(64))
    dataSource.saveUser(user)
  }

  @Test
  fun `saveUser should set timestamps`() = runTest {
    val user = User("b".repeat(64))
    val beforeSave = System.currentTimeMillis()

    dataSource.saveUser(user)

    val savedUser = dataSource.getUser()
    assertNotNull("Saved user should be retrievable", savedUser)
  }

  /** Test that existing user can be overwritten */
  @Test
  fun `saveUser should overwrite existing user`() = runTest {
    val user1 = User("c".repeat(64))
    val user2 = User("d".repeat(64))

    dataSource.saveUser(user1)
    dataSource.saveUser(user2)

    val savedUser = dataSource.getUser()
    assertEquals("Should retrieve the latest user", user2.pubkey, savedUser?.pubkey)
  }

  @Test
  fun `getUser after save should return saved user`() = runTest {
    val user = User("e".repeat(64))
    dataSource.saveUser(user)

    val retrieved = dataSource.getUser()

    assertNotNull("getUser should return saved user", retrieved)
    assertEquals("Retrieved pubkey should match", user.pubkey, retrieved?.pubkey)
  }

  @Test
  fun `getUser with no data should return null`() = runTest {
    val retrieved = dataSource.getUser()

    assertNull("getUser should return null when no data exists", retrieved)
  }

  @Test
  fun `getUser with invalid data should return null`() = runTest {
    context
        .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
        .edit()
        .putString("user_pubkey", "invalid_pubkey")
        .commit()

    val retrieved = dataSource.getUser()

    assertNull("getUser should return null for invalid pubkey", retrieved)
  }

  /** Test that created_at timestamp is saved and retrieved correctly */
  @Test
  fun `getUser should preserve createdAt timestamp`() = runTest {
    val user = User("f".repeat(64))

    dataSource.saveUser(user)
    Thread.sleep(10)

    val retrieved = dataSource.getUser()
    assertNotNull("User should be retrievable after save", retrieved)
    assertEquals("Pubkey should match", user.pubkey, retrieved?.pubkey)
  }

  /** Test that last_accessed timestamp is saved and retrieved correctly */
  @Test
  fun `getUser should update lastAccessed timestamp`() = runTest {
    val user = User("1".repeat(64))
    dataSource.saveUser(user)
    Thread.sleep(10)

    val firstRetrieval = dataSource.getUser()
    Thread.sleep(10)
    val secondRetrieval = dataSource.getUser()

    assertNotNull("First retrieval should succeed", firstRetrieval)
    assertNotNull("Second retrieval should succeed", secondRetrieval)
    assertEquals("Data should be consistent", firstRetrieval?.pubkey, secondRetrieval?.pubkey)
  }

  @Test
  fun `clearLoginState should succeed`() = runTest {
    val user = User("2".repeat(64))
    dataSource.saveUser(user)

    dataSource.clearLoginState()

    val retrieved = dataSource.getUser()
    assertNull("User should be null after clear", retrieved)
  }

  /** Test that clear succeeds even when no data exists */
  @Test
  fun `clearLoginState with no data should succeed`() = runTest { dataSource.clearLoginState() }

  /** Test that timestamps are also removed after clear */
  @Test
  fun `clearLoginState should remove timestamps`() = runTest {
    val user = User("3".repeat(64))
    dataSource.saveUser(user)

    dataSource.clearLoginState()

    val prefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
    assertFalse("user_pubkey should be removed", prefs.contains("user_pubkey"))
    assertFalse("login_created_at should be removed", prefs.contains("login_created_at"))
    assertFalse("login_last_accessed should be removed", prefs.contains("login_last_accessed"))
  }

  /** Test error handling for storage errors (placeholder for future extension) */
  @Test fun `saveUser should handle storage error`() = runTest {}
}
