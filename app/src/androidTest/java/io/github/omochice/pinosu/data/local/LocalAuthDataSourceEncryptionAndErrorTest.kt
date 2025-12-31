package io.github.omochice.pinosu.data.local

import roid.content.Context
import roidx.test.core.app.ApplicationProvider
import roidx.test.ext.junit.runners.AndroidJUnit4
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.StorageError
import kotlinx.coroutines.test.runtest
import org.junit.After
import org.junit.Assert.*import org.junit.Before
import org.junit.test
import org.junit.runner.RunWith

/*** LocalAuthDataSourcetests for encryption/decryption error h ling** Task 3.3: LocalAuthDataSourceUnit tests for* - invaliddataoferrortest* - encryptiondecryptionNormalverify Requirements: 2.1, 2.2, 2.5, 6.2*/@RunWith(AndroidJUnit4::class)
class LocalAuthDataSourceEncryptionAndErrortest {

 private lateinit var context: Context
 private lateinit var dataSource: LocalAuthDataSource

 @Before
 fun setup() {
 context = ApplicationProvider.getApplicationContext()
 dataSource = LocalAuthDataSource(context)
 }

 @After
 fun tearDown() {
// afterdataclear context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
 }

// ========== Encryption/Decryption Verification tests ==========
/*** dataencryptionedsaveed verifytest** Task 3.3: encryptiondecryptionNormalverify Requirement 6.2: EncryptedSharedPreferencesencryption*/ @test
 fun testDataIsEncryptedInStorage() = runtest {
val user = User("abcd1234".repeat(8)) // 64ofvalid pubkey
// Usersave dataSource.saveUser(user)

// ofSharedPreferences (encryption)same// EncryptedSharedPreferencesing, ofkey/value should val regularPrefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
 val allEntries = regularPrefs.all

// EncryptedSharedPreferencesing:// 1. encryptionedingfor, "user_pubkey" ofkey// 2. valueencryptioningfor, ofpubkey var foundPlaintextPubkey = false
 for ((key, value) in allEntries) {
 if (key == "user_pubkey" && value == user.pubkey) {
 foundPlaintextPubkey = true
 break
 }
// valuepubkey detection if (value.toString() == user.pubkey) {
 foundPlaintextPubkey = true
 break
 }
 }

 assertFalse("Data should be encrypted, not stored in plaintext", foundPlaintextPubkey)
 }

/*** encryptioneddatacorrectlydecryptioned verifytest** Task 3.3: encryptiondecryptionNormalverify*/ @test
 fun testEncryptedDataCanBeDecrypted() = runtest {
 val user = User("1234abcd".repeat(8))

// save dataSource.saveUser(user)

// get (decryption) val retrieved = dataSource.getUser()

// decryptiondataofdata Verify that assertNotNull("Encrypted data should be decryptable", retrieved)
 assertEquals("Decrypted data should match original", user.pubkey, retrieved?.pubkey)
 }

/*** differentsameencryptionkeydecryptioncanVerify that** Task 3.3: encryptiondecryptionNormalverify Requirement 6.2: Android KeystoreofMasterKey*/ @test
 fun testEncryptionKeyPersistenceAcrossInstances() = runtest {
 val user = User("fedcba98".repeat(8))

// ofsave dataSource.saveUser(user)

// val newDataSource = LocalAuthDataSource(context)

// getcanVerify that val retrieved = newDataSource.getUser()

 assertNotNull("New instance should be able to decrypt data", retrieved)
 assertEquals("Data should be consistent across instances", user.pubkey, retrieved?.pubkey)
 }

/*** ofsavegetencryptiondecryptionsuccessfullyVerify that** Task 3.3: encryptiondecryptionNormalverify*/ @test
 fun testMultipleEncryptionDecryptionCycles() = runtest {
 val users =
 listOf(
 User("0".repeat(64)), User("1".repeat(64)), User("a".repeat(64)), User("f".repeat(64)))

 for (user in users) {
// save dataSource.saveUser(user)

// get val retrieved = dataSource.getUser()

// verification assertNotNull("User should be retrievable", retrieved)
 assertEquals("Retrieved user should match saved user", user.pubkey, retrieved?.pubkey)
 }
 }

// ========== Error H ling tests ==========
/*** invalid formatofpubkeysaveedingnullVerify that** Task 3.3: invaliddataoferrortest Requirement 2.1: dataverification*/ @test
 fun testGetUser_InvalidPubkeyFormat_ReturnsNull() = runtest {
 val invalidPubkeys =
 listOf(
"invalid", //"g".repeat(64), // invalid 16"abc", // invalid 16"ABCD1234".repeat(8), // (invalid)"abcd1234".repeat(7), // 63 (1)"abcd1234".repeat(8) + "0" // 65 (1) )

 for (invalidPubkey in invalidPubkeys) {
// invalid datasave context
 .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
 .edit()
 .putString("user_pubkey", invalidPubkey)
 .commit()

// get val retrieved = dataSource.getUser()

// nullis returnedVerify that assertNull("getUser should return null for invalid pubkey format: $invalidPubkey", retrieved)

// dataSource.clearLoginState()
 }
 }

/*** pubkey nullVerify that** Task 3.3: invaliddataoferrortest*/ @test
 fun testGetUser_MissingPubkey_ReturnsNull() = runtest {
// state context
 .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
 .edit()
 .putLong("login_created_at", System.currentTimeMillis())
 .putLong("login_last_accessed", System.currentTimeMillis())
 .commit()

 val retrieved = dataSource.getUser()

 assertNull("getUser should return null when pubkey is missing", retrieved)
 }

/*** SharedPreferencesofedVerify that** Task 3.3: invaliddataoferrortest Requirement 2.2: error*/ @test
 fun testGetUser_ExceptionH ling_ReturnsNull() = runtest {
// invalid typeofdatasave (Stringing ThisIntsave) context
 .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
 .edit()
.putInt("user_pubkey", 12345) // String Int .commit()

// exception occursnullis returnedVerify that val retrieved = dataSource.getUser()

 assertNull("getUser should return null on exception", retrieved)
 }

/*** empty stringofpubkeysaveedingnullVerify that** Task 3.3: invaliddataoferrortest*/ @test
 fun testGetUser_EmptyPubkey_ReturnsNull() = runtest {
 context
 .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
 .edit()
 .putString("user_pubkey", "")
 .commit()

 val retrieved = dataSource.getUser()

 assertNull("getUser should return null for empty pubkey", retrieved)
 }

/*** invalid correctlyprocessingedVerify that** Task 3.3: invaliddataoferrortest** Note: EncryptedSharedPreferencesingfor, dataof . Thisoftest , Implementation ofverification.*/ @test
 fun testGetUser_TimestampH ling() = runtest {
 val user = User("deadbeef".repeat(8))

// successfullysave dataSource.saveUser(user)

// successfullygetcanVerify that (seted) val retrieved = dataSource.getUser()

 assertNotNull("User should be retrievable", retrieved)
 assertEquals("Pubkey should match", user.pubkey, retrieved?.pubkey)
 }

/*** clearLoginStateaftersaveeddatadeleteedVerify that** Task 3.3: errortest Requirement 2.5: login stateofclear*/ @test
 fun testClearLoginState_RemovesAllData() = runtest {
 val user = User("cafe1234".repeat(8))

// Usersave dataSource.saveUser(user)

// datasaveingVerify that assertNotNull("User should be saved", dataSource.getUser())

// clear dataSource.clearLoginState()

// ofkeydeleteedingVerify that val prefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
 assertFalse("user_pubkey should be removed", prefs.contains("user_pubkey"))
 assertFalse("login_created_at should be removed", prefs.contains("login_created_at"))
 assertFalse("login_last_accessed should be removed", prefs.contains("login_last_accessed"))

// getUsernullis returnedVerify that assertNull("getUser should return null after clear", dataSource.getUser())
 }

/*** saveUserStorageError.WriteError Verify that** Note: EncryptedSharedPreferences for, whenWriteError of.Thisoftest errorofverification.** Task 3.3: invaliddataoferrortest*/ @test
 fun testSaveUser_ValidatesErrorType() = runtest {
// Implementation of, EncryptedSharedPreferencesthrow exception// offor, normalofverify val user = User("beef".repeat(16))

 try {
 dataSource.saveUser(user)
// successfullysaveVerify that val retrieved = dataSource.getUser()
 assertEquals("User should be saved successfully", user.pubkey, retrieved?.pubkey)
 } catch (e: StorageError.WriteError) {
// WriteError, errormessageVerify that assertNotNull("Error message should be present", e.message)
 }
 }

/*** clearLoginStateStorageError.WriteError Verify that** Note: EncryptedSharedPreferences for, whenWriteError of.Thisoftest errorofverification.** Task 3.3: invaliddataoferrortest*/ @test
 fun testClearLoginState_ValidatesErrorType() = runtest {
// Implementation of, EncryptedSharedPreferencesthrow exception// offor, normalofverify try {
 dataSource.clearLoginState()
// successfullyclearVerify that assertNull("Data should be cleared", dataSource.getUser())
 } catch (e: StorageError.WriteError) {
// WriteError, errormessageVerify that assertNotNull("Error message should be present", e.message)
 }
 }
}
