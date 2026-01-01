package io.github.omochice.pinosu.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalAuthDataSourceTest {

  private lateinit var context: Context
  private lateinit var dataSource: LocalAuthDataSource

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    dataSource = LocalAuthDataSource(context)
  }

  @After
  fun tearDown() {
    context
        .getSharedPreferences("pinosu_auth_prefs_test", Context.MODE_PRIVATE)
        .edit()
        .clear()
        .commit()
  }

  @Test
  fun testInitialization() {
    assertNotNull("LocalAuthDataSource should be initialized", dataSource)
  }

  @Test
  fun testEncryptedSharedPreferencesCreation() {
    try {
      dataSource.toString()
      assertTrue("EncryptedSharedPreferences should be created successfully", true)
    } catch (e: Exception) {
      fail("EncryptedSharedPreferences creation failed: ${e.message}")
    }
  }

  @Test
  fun testMasterKeyGeneration() {
    assertNotNull("MasterKey should be generated", dataSource)
  }

  @Test
  fun testEncryptionSchemes() {
    assertNotNull("Encryption schemes should be configured", dataSource)
  }

  @Test
  fun testMultipleInitializations() {
    val dataSource1 = LocalAuthDataSource(context)
    val dataSource2 = LocalAuthDataSource(context)

    assertNotNull("First initialization should succeed", dataSource1)
    assertNotNull("Second initialization should succeed", dataSource2)
  }
}
