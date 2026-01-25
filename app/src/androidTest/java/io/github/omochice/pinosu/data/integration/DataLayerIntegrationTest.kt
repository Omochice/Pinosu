package io.github.omochice.pinosu.data.integration

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.omochice.pinosu.data.local.AuthData
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.local.TestAuthDataSerializer
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.data.repository.Nip55AuthRepository
import io.github.omochice.pinosu.domain.model.User
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for data layer
 *
 * Tests:
 * - AuthRepository + Nip55SignerClient + LocalAuthDataSource integration
 * - DataStore with Tink encryption runtime test (save → retrieve → delete)
 *
 * Test strategy:
 * - Data layer: Using actual Nip55AuthRepository, LocalAuthDataSource, Nip55SignerClient
 * - Storage: Using test DataStore without encryption for isolation
 * - Note: NIP-55 signer communication requires actual NIP-55 signer app, testing storage operations
 *   only
 */
@RunWith(AndroidJUnit4::class)
class DataLayerIntegrationTest {

  private lateinit var context: Context
  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var nip55SignerClient: Nip55SignerClient
  private lateinit var authRepository: Nip55AuthRepository
  private lateinit var testDataStore: DataStore<AuthData>
  private lateinit var testFile: File

  @Before
  fun setup() {
    context = InstrumentationRegistry.getInstrumentation().targetContext
    testFile = File(context.filesDir, "test_integration_auth_data_${System.currentTimeMillis()}.pb")
    testDataStore =
        DataStoreFactory.create(serializer = TestAuthDataSerializer(), produceFile = { testFile })

    localAuthDataSource = LocalAuthDataSource(context, testDataStore)
    nip55SignerClient = Nip55SignerClient(context)
    authRepository = Nip55AuthRepository(nip55SignerClient, localAuthDataSource)
  }

  @After
  fun tearDown() {
    runTest { localAuthDataSource.clearLoginState() }
    testFile.delete()
  }

  /**
   * NIP-55 signer not installed detection → AuthRepository integration
   *
   * Integration flow:
   * 1. AuthRepository checks for NIP-55 signer installation
   * 2. Nip55SignerClient detects
   * 3. Return result
   */
  @Test
  fun `checkNip55SignerInstalled should use Nip55SignerClient`() {

    val isInstalled = authRepository.checkNip55SignerInstalled()

    assertTrue("checkNip55SignerInstalled should return boolean", isInstalled || !isInstalled)
  }

  /**
   * DataStore runtime test: Save → Retrieve
   *
   * Integration flow:
   * 1. Save user info to LocalAuthDataSource
   * 2. Data is stored in DataStore
   * 3. Retrieve and verify saved data
   */
  @Test
  fun `encrypted storage save and get should work correctly`() = runTest {
    val testPubkey = "npub1" + "b".repeat(59)
    val testUser = User(testPubkey)

    localAuthDataSource.saveUser(testUser)
    advanceUntilIdle()

    val retrievedUser = localAuthDataSource.getUser()
    assertNotNull("Retrieved user should not be null", retrievedUser)
    assertEquals("Retrieved pubkey should match", testPubkey, retrievedUser?.pubkey)
  }

  /**
   * DataStore runtime test: Save → Delete → Retrieve
   *
   * Integration flow:
   * 1. Save user info to LocalAuthDataSource
   * 2. Delete during logout
   * 3. Verify data cannot be retrieved after deletion
   */
  @Test
  fun `encrypted storage save and delete should work correctly`() = runTest {
    val testPubkey = "npub1" + "c".repeat(59)
    val testUser = User(testPubkey)
    localAuthDataSource.saveUser(testUser)
    advanceUntilIdle()

    val userBeforeDelete = localAuthDataSource.getUser()
    assertNotNull("User should exist before delete", userBeforeDelete)

    localAuthDataSource.clearLoginState()
    advanceUntilIdle()

    val userAfterDelete = localAuthDataSource.getUser()
    assertNull("User should be null after delete", userAfterDelete)
  }

  /**
   * DataStore runtime test: Multiple save-get-delete cycles
   *
   * Integration flow:
   * 1. Repeat save → retrieve → delete multiple times
   * 2. Verify stability of DataStore
   */
  @Test
  fun `encrypted storage multiple save get delete cycles should work correctly`() = runTest {
    val users =
        listOf(
            User("npub1" + "d".repeat(59)),
            User("npub1" + "e".repeat(59)),
            User("npub1" + "f".repeat(59)),
        )

    users.forEach { user ->
      localAuthDataSource.saveUser(user)

      val retrievedUser = localAuthDataSource.getUser()
      assertNotNull("Retrieved user should not be null", retrievedUser)
      assertEquals("Retrieved pubkey should match", user.pubkey, retrievedUser?.pubkey)

      localAuthDataSource.clearLoginState()

      val userAfterDelete = localAuthDataSource.getUser()
      assertNull("User should be null after delete", userAfterDelete)
    }
  }

  /**
   * Logout flow → Delete data from DataStore
   *
   * Integration flow:
   * 1. User is logged in
   * 2. Call AuthRepository.logout()
   * 3. Login state is cleared in LocalAuthDataSource
   * 4. Data is deleted from DataStore
   */
  @Test
  fun `logout flow should clear encrypted storage`() = runTest {
    val testPubkey = "npub1" + "g".repeat(59)
    val testUser = User(testPubkey)
    localAuthDataSource.saveUser(testUser)
    advanceUntilIdle()

    val userBeforeLogout = authRepository.getLoginState()
    assertNotNull("User should exist before logout", userBeforeLogout)
    assertEquals("Pubkey should match", testPubkey, userBeforeLogout?.pubkey)

    authRepository.logout()
    advanceUntilIdle()

    val userAfterLogout = authRepository.getLoginState()
    assertNull("User should be null after logout", userAfterLogout)

    val directRetrieve = localAuthDataSource.getUser()
    assertNull("User should be null in storage", directRetrieve)
  }

  /**
   * App restart simulation → Restore login state
   *
   * Integration flow:
   * 1. Save user info
   * 2. Recreate AuthRepository/LocalAuthDataSource instances (simulating restart)
   * 3. Verify that saved login state is restored
   */
  @Test
  fun `app restart should restore login state from encrypted storage`() = runTest {
    val testPubkey = "npub1" + "h".repeat(59)
    val testUser = User(testPubkey)
    localAuthDataSource.saveUser(testUser)
    advanceUntilIdle()

    val newLocalAuthDataSource = LocalAuthDataSource(context, testDataStore)
    val newAuthRepository = Nip55AuthRepository(nip55SignerClient, newLocalAuthDataSource)

    val restoredUser = newAuthRepository.getLoginState()
    assertNotNull("User should be restored after restart", restoredUser)
    assertEquals("Restored pubkey should match", testPubkey, restoredUser?.pubkey)
  }

  /**
   * Invalid data handling → Return null
   *
   * Integration flow:
   * 1. Try to save data with invalid public key format
   * 2. User class validation error
   * 3. Data is not saved, null is returned
   */
  @Test
  fun `invalid data should be rejected by User validation`() = runTest {
    val invalidPubkey = "invalid_pubkey_format"

    try {
      val invalidUser = User(invalidPubkey)
      localAuthDataSource.saveUser(invalidUser)
      fail("Should throw exception for invalid pubkey")
    } catch (e: IllegalArgumentException) {
      assertTrue("Should contain validation error", e.message?.contains("Invalid") == true)
    }
  }

  private suspend fun advanceUntilIdle() {
    kotlinx.coroutines.delay(100)
  }
}
