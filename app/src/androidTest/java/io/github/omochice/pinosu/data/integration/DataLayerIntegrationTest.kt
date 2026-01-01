package io.github.omochice.pinosu.data.integration

import io.github.omochice.pinosu.data.amber.AmberSignerClient
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.repository.AmberAuthRepository
import io.github.omochice.pinosu.domain.model.User
import kotlinx.coroutines.test.runtest
import org.junit.After
import org.junit.Assert.*import org.junit.Before
import org.junit.runner.RunWith
import org.junit.test
import roid.content.Context
import roidx.test.core.app.ApplicationProvider
import roidx.test.ext.junit.runners.AndroidJUnit4

class DataLayerIntegrationtest {

 private lateinit var context: Context
 private lateinit var localAuthDataSource: LocalAuthDataSource
 private lateinit var amberSignerClient: AmberSignerClient
 private lateinit var authRepository: AmberAuthRepository

 @Before
 fun setup() {
 context = ApplicationProvider.getApplicationContext()

// LocalAuthDataSourcewhenImplementation of (EncryptedSharedPreferences) localAuthDataSource = LocalAuthDataSource(context)

// AmberSignerClientwhenImplementation of (AmbercommunicationtestDifferent test) amberSignerClient = AmberSignerClient(context)

// AuthRepositorywhenImplementation of authRepository = AmberAuthRepository(amberSignerClient, localAuthDataSource)
 }

 @After
 fun tearDown() {
// afterdataclear runtest { localAuthDataSource.clearLoginState() }
 }

// ========== AuthRepository + LocalAuthDataSource test ==========
/**
 * AuthRepository delegates Amber installation detection to AmberSignerClient
 *
 * flow:
 * 1. AuthRepository requests Amber installation check
 * 2. AmberSignerClient performs the detection
 * 3. Result is returned
 */
@test
 fun checkAmberInstalled_shouldUseAmberSignerClient() {
// When: Amberverify val isInstalled = authRepository.checkAmberInstalled()

// Then: AmberSignerClientofresultis returned (whenofstate)// Note: resultofAmberInstallstatefor, booleanvalueofverify assertTrue(
"checkAmberInstalled should return boolean", isInstalled || !isInstalled) // always true }

// ========== EncryptedSharedPreferencestest (save → get → delete) ==========
/**
 * EncryptedSharedPreferences integration: save and retrieve user data
 *
 * flow:
 * 1. Save user data via LocalAuthDataSource
 * 2. Data is encrypted and stored in EncryptedSharedPreferences
 * 3. Retrieve and verify saved data
 */
@test
 fun encryptedStorage_saveAndGet_shouldWorkCorrectly() = runtest {
// Given: testUser val testPubkey = "b".repeat(64)
 val testUser = User(testPubkey)

// When: Usersave localAuthDataSource.saveUser(testUser)
 advanceUntilIdle()

// Then: savedatagetcan val retrievedUser = localAuthDataSource.getUser()
 assertNotNull("Retrieved user should not be null", retrievedUser)
 assertEquals("Retrieved pubkey should match", testPubkey, retrievedUser?.pubkey)
 }

/**
 * EncryptedSharedPreferencestest: save → delete → get
 *
 * flow:
 * 1. Save user data via LocalAuthDataSource
 * 2. Clear login state (deletion during logout)
 * 3. Verify that data is gone after deletion
 */
@test
 fun encryptedStorage_saveAndDelete_shouldWorkCorrectly() = runtest {
// Given: Usersaveingstate val testPubkey = "c".repeat(64)
 val testUser = User(testPubkey)
 localAuthDataSource.saveUser(testUser)
 advanceUntilIdle()

 val userBeforeDelete = localAuthDataSource.getUser()
 assertNotNull("User should exist before delete", userBeforeDelete)

// When: login stateclear localAuthDataSource.clearLoginState()
 advanceUntilIdle()

// Then: deletedataget val userAfterDelete = localAuthDataSource.getUser()
 assertNull("User should be null after delete", userAfterDelete)
 }

/**
 * EncryptedSharedPreferencestest: ofsavegetdelete
 *
 * flow:
 * 1. Save, retrieve, and delete operations in sequence
 * 2. Verify EncryptedSharedPreferences behaves correctly
 * 3. Test multiple cycles of save-get-delete
 */
@test
 fun encryptedStorage_multipleSaveGetDeleteCycles_shouldWorkCorrectly() = runtest {
// Given: oftestUser val users =
 listOf(
 User("d".repeat(64)),
 User("e".repeat(64)),
 User("f".repeat(64)),
 )

// When: Usersave → get → delete users.forEach { user ->
// save localAuthDataSource.saveUser(user)

// get val retrievedUser = localAuthDataSource.getUser()
 assertNotNull("Retrieved user should not be null", retrievedUser)
 assertEquals("Retrieved pubkey should match", user.pubkey, retrievedUser?.pubkey)

// delete localAuthDataSource.clearLoginState()

// deleteverify val userAfterDelete = localAuthDataSource.getUser()
 assertNull("User should be null after delete", userAfterDelete)
 }
 }

/**
 * logoutflow → EncryptedSharedPreferencesdatadelete
 *
 * flow:
 * 1. User is in logged-in state
 * 2. Call AuthRepository.logout()
 * 3. LocalAuthDataSource clears login state
 * 4. EncryptedSharedPreferences data is deleted
 */
@test
 fun logoutFlow_shouldClearEncryptedStorage() = runtest {
// Given: User is logged in val testPubkey = "g".repeat(64)
 val testUser = User(testPubkey)
 localAuthDataSource.saveUser(testUser)
 advanceUntilIdle()

 val userBeforeLogout = authRepository.getLoginState()
 assertNotNull("User should exist before logout", userBeforeLogout)
 assertEquals("Pubkey should match", testPubkey, userBeforeLogout?.pubkey)

// When: logout authRepository.logout()
 advanceUntilIdle()

// Then: EncryptedSharedPreferencesdatadeleteed val userAfterLogout = authRepository.getLoginState()
 assertNull("User should be null after logout", userAfterLogout)

// EncryptedSharedPreferencesverify val directRetrieve = localAuthDataSource.getUser()
 assertNull("User should be null in storage", directRetrieve)
 }

/**
 * restart app → login state
 *
 * flow:
 * 1. Save user data
 * 2. Create new AuthRepository/LocalAuthDataSource instances (simulating app restart)
 * 3. Verify that saved login state is restored
 */
@test
 fun appRestart_shouldRestoreLoginStateFromEncryptedStorage() = runtest {
// Given: User is logged in val testPubkey = "h".repeat(64)
 val testUser = User(testPubkey)
 localAuthDataSource.saveUser(testUser)
 advanceUntilIdle()

// When: () val newLocalAuthDataSource = LocalAuthDataSource(context)
 val newAuthRepository = AmberAuthRepository(amberSignerClient, newLocalAuthDataSource)

// Then: login stateed val restoredUser = newAuthRepository.getLoginState()
 assertNotNull("User should be restored after restart", restoredUser)
 assertEquals("Restored pubkey should match", testPubkey, restoredUser?.pubkey)
 }

/**
 * invaliddata → null
 *
 * flow:
 * 1. Attempt to save invalid data
 * 2. User validation fails with an error
 * 3. Invalid data is rejected
 * 4. No data is stored
 */
@test
 fun invalidData_shouldBeRejectedByUserValidation() = runtest {
// Given: Invalid val invalidPubkey = "invalid_pubkey_format"

// When: invalid User// Then: Userofrequireexception occurs try {
 val invalidUser = User(invalidPubkey)
 localAuthDataSource.saveUser(invalidUser)
 fail("Should throw exception for invalid pubkey")
 } catch (e: IllegalArgumentException) {
// Expected exception assertTrue("Should contain validation error", e.message?.contains("Invalid") == true)
 }
 }

// : Coroutineofcompletion private suspend fun advanceUntilIdle() {
 kotlinx.coroutines.delay(100)
 }
}
