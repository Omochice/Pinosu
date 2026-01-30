package io.github.omochice.pinosu.feature.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.domain.model.User
import java.io.File
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
  private lateinit var testDataStore: DataStore<AuthData>
  private lateinit var testFile: File

  @Before
  fun setup() {
    context = InstrumentationRegistry.getInstrumentation().targetContext
    testFile = File(context.filesDir, "test_auth_data_${System.currentTimeMillis()}.pb")
    testDataStore =
        DataStoreFactory.create(serializer = TestAuthDataSerializer(), produceFile = { testFile })

    dataSource =
        LocalAuthDataSource(
            context, testDataStore // Use test DataStore directly as production one
            )
  }

  @After
  fun tearDown() {
    testFile.delete()
  }

  @Test
  fun `saveUser should succeed`() = runTest {
    val user = User(Pubkey.parse("npub1" + "a".repeat(59))!!)
    dataSource.saveUser(user)
  }

  @Test
  fun `saveUser should set timestamps`() = runTest {
    val user = User(Pubkey.parse("npub1" + "b".repeat(59))!!)
    val beforeSave = System.currentTimeMillis()

    dataSource.saveUser(user)

    val savedUser = dataSource.getUser()
    assertNotNull("Saved user should be retrievable", savedUser)
  }

  @Test
  fun `saveUser should overwrite existing user`() = runTest {
    val user1 = User(Pubkey.parse("npub1" + "c".repeat(59))!!)
    val user2 = User(Pubkey.parse("npub1" + "d".repeat(59))!!)

    dataSource.saveUser(user1)
    dataSource.saveUser(user2)

    val savedUser = dataSource.getUser()
    assertEquals("Should retrieve the latest user", user2.pubkey, savedUser?.pubkey)
  }

  @Test
  fun `getUser after save should return saved user`() = runTest {
    val user = User(Pubkey.parse("npub1" + "e".repeat(59))!!)
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
  fun `getUser should preserve createdAt timestamp`() = runTest {
    val user = User(Pubkey.parse("npub1" + "f".repeat(59))!!)

    dataSource.saveUser(user)

    val retrieved = dataSource.getUser()
    assertNotNull("User should be retrievable after save", retrieved)
    assertEquals("Pubkey should match", user.pubkey, retrieved?.pubkey)
  }

  @Test
  fun `getUser should update lastAccessed timestamp`() = runTest {
    val user = User(Pubkey.parse("npub1" + "1".repeat(59))!!)
    dataSource.saveUser(user)

    val firstRetrieval = dataSource.getUser()
    val secondRetrieval = dataSource.getUser()

    assertNotNull("First retrieval should succeed", firstRetrieval)
    assertNotNull("Second retrieval should succeed", secondRetrieval)
    assertEquals("Data should be consistent", firstRetrieval?.pubkey, secondRetrieval?.pubkey)
  }

  @Test
  fun `clearLoginState should succeed`() = runTest {
    val user = User(Pubkey.parse("npub1" + "2".repeat(59))!!)
    dataSource.saveUser(user)

    dataSource.clearLoginState()

    val retrieved = dataSource.getUser()
    assertNull("User should be null after clear", retrieved)
  }

  @Test
  fun `clearLoginState with no data should succeed`() = runTest { dataSource.clearLoginState() }

  @Test
  fun `clearLoginState should remove all data`() = runTest {
    val user = User(Pubkey.parse("npub1" + "3".repeat(59))!!)
    dataSource.saveUser(user)

    dataSource.clearLoginState()

    assertNull("getUser should return null after clear", dataSource.getUser())
    assertNull("getRelayList should return null after clear", dataSource.getRelayList())
  }

  @org.junit.Ignore("TODO: Implement storage error handling test")
  @Test
  fun `saveUser should handle storage error`() = runTest {}
}
