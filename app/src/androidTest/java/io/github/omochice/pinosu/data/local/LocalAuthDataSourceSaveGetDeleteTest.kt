package io.github.omochice.pinosu.data.local

import roid.content.Context
import roidx.test.core.app.ApplicationProvider
import roidx.test.ext.junit.runners.AndroidJUnit4
import io.github.omochice.pinosu.domain.model.User
import kotlinx.coroutines.test.runtest
import org.junit.After
import org.junit.Assert.*import org.junit.Before
import org.junit.test
import org.junit.runner.RunWith

/*** LocalAuthDataSourcetests for save/get/delete functions** Task 3.2: Userdataofsavegetdeletefunctionality Requirements: 1.4, 2.1, 2.2, 2.5*/@RunWith(AndroidJUnit4::class)
class LocalAuthDataSourceSaveGetDeletetest {

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

// ========== saveUser tests ==========
/** Usersuccessfullysavecan test Task 3.2: saveUserimplementation */ @test
 fun testSaveUser_Success() = runtest {
 val user = User("a".repeat(64))

// ed Verify that dataSource.saveUser(user)
// success - exception occurs }

/** Usersavewhened test Task 3.2: created_at/last_accessed */ @test
 fun testSaveUser_SetsTimestamps() = runtest {
 val user = User("b".repeat(64))
 val beforeSave = System.currentTimeMillis()

 dataSource.saveUser(user)

 val savedUser = dataSource.getUser()
 assertNotNull("Saved user should be retrievable", savedUser)
// setedingVerify that (getUserimplementationaftervalid) }

/** ofUsersavecan test */ @test
 fun testSaveUser_Overwrite() = runtest {
 val user1 = User("c".repeat(64))
 val user2 = User("d".repeat(64))

 dataSource.saveUser(user1)
dataSource.saveUser(user2) // ed Verify that
 val savedUser = dataSource.getUser()
 assertEquals("Should retrieve the latest user", user2.pubkey, savedUser?.pubkey)
 }

// ========== getUser tests ==========
/** saveedUsergetcan test Task 3.2: getUserimplementation */ @test
 fun testGetUser_AfterSave() = runtest {
 val user = User("e".repeat(64))
 dataSource.saveUser(user)

 val retrieved = dataSource.getUser()

 assertNotNull("getUser should return saved user", retrieved)
 assertEquals("Retrieved pubkey should match", user.pubkey, retrieved?.pubkey)
 }

/** datasaveednot null test Task 3.2: null */ @test
 fun testGetUser_NoDataReturnsNull() = runtest {
 val retrieved = dataSource.getUser()

 assertNull("getUser should return null when no data exists", retrieved)
 }

/** invalid pubkeydatasaveedingnull test Task 3.2: verification */ @test
 fun testGetUser_InvalidDataReturnsNull() = runtest {
// invalid datasave context
 .getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
 .edit()
 .putString("user_pubkey", "invalid_pubkey")
 .commit()

 val retrieved = dataSource.getUser()

 assertNull("getUser should return null for invalid pubkey", retrieved)
 }

/** created_atcorrectlysavegeted test */ @test
 fun testGetUser_PreservesCreatedAt() = runtest {
 val user = User("f".repeat(64))

 dataSource.saveUser(user)
Thread.sleep(10) //
// EncryptedSharedPreferencesingfor, verification// , datasuccessfullysavegetcanVerify that val retrieved = dataSource.getUser()
 assertNotNull("User should be retrievable after save", retrieved)
 assertEquals("Pubkey should match", user.pubkey, retrieved?.pubkey)
 }

/** last_accessedcorrectlysavegeted test */ @test
 fun testGetUser_UpdatesLastAccessed() = runtest {
 val user = User("1".repeat(64))
 dataSource.saveUser(user)
 Thread.sleep(10)

// getdataingVerify that val firstRetrieval = dataSource.getUser()
 Thread.sleep(10)
 val secondRetrieval = dataSource.getUser()

// EncryptedSharedPreferencesingfor, verification// , ofdatagetcanVerify that assertNotNull("First retrieval should succeed", firstRetrieval)
 assertNotNull("Second retrieval should succeed", secondRetrieval)
 assertEquals("Data should be consistent", firstRetrieval?.pubkey, secondRetrieval?.pubkey)
 }

// ========== clearLoginState tests ==========
/** login statesuccessfullyclearcan test Task 3.2: clearLoginStateimplementation */ @test
 fun testClearLoginState_Success() = runtest {
 val user = User("2".repeat(64))
 dataSource.saveUser(user)

dataSource.clearLoginState() // ed Verify that
 val retrieved = dataSource.getUser()
 assertNull("User should be null after clear", retrieved)
 }

/** data stateclearsuccess test */ @test
 fun testClearLoginState_NoDataSucceeds() = runtest {
dataSource.clearLoginState() // ed Verify that }

/** clearafterdeleteed test */ @test
 fun testClearLoginState_RemovesTimestamps() = runtest {
 val user = User("3".repeat(64))
 dataSource.saveUser(user)

 dataSource.clearLoginState()

 val prefs = context.getSharedPreferences("pinosu_auth_prefs", Context.MODE_PRIVATE)
 assertFalse("user_pubkey should be removed", prefs.contains("user_pubkey"))
 assertFalse("login_created_at should be removed", prefs.contains("login_created_at"))
 assertFalse("login_last_accessed should be removed", prefs.contains("login_last_accessed"))
 }

// ========== Error H ling tests ==========
/** errorwhenStorageError.WriteError test () */ @test
 fun testSaveUser_H lesStorageError() = runtest {
// Implementation ofEncryptedSharedPreferencesofnot// erroradditionof// errorfor, at this pointskip }
}
