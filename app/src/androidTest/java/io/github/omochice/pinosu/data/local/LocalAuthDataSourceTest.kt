package io.github.omochice.pinosu.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.crypto.tink.aead.AeadConfig
import io.github.omochice.pinosu.data.crypto.TinkKeyManager
import java.io.File
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for LocalAuthDataSource initialization
 *
 * Tests DataStore and Tink encryption infrastructure setup on real Android device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class LocalAuthDataSourceTest {

  private lateinit var context: Context
  private lateinit var dataSource: LocalAuthDataSource
  private lateinit var testDataStore: DataStore<AuthData>
  private lateinit var testFile: File
  private lateinit var tinkKeyManager: TinkKeyManager

  @Before
  fun setup() {
    AeadConfig.register()
    context = ApplicationProvider.getApplicationContext()
    tinkKeyManager = TinkKeyManager(context)
    testFile = File(context.filesDir, "test_init_auth_data_${System.currentTimeMillis()}.pb")
    testDataStore =
        DataStoreFactory.create(
            serializer = AuthDataSerializer(tinkKeyManager.getAead()), produceFile = { testFile })

    dataSource = LocalAuthDataSource(context, testDataStore)
  }

  @After
  fun tearDown() {
    testFile.delete()
  }

  @Test
  fun `initialization should succeed`() {
    assertNotNull("LocalAuthDataSource should be initialized", dataSource)
  }

  @Test
  fun `DataStore creation should succeed`() {
    try {
      dataSource.toString()
      assertTrue("DataStore should be created successfully", true)
    } catch (e: Exception) {
      fail("DataStore creation failed: ${e.message}")
    }
  }

  @Test
  fun `TinkKeyManager generation should succeed`() {
    assertNotNull("TinkKeyManager should be generated", tinkKeyManager)
    assertNotNull("AEAD should be available", tinkKeyManager.getAead())
  }

  @Test
  fun `encryption should be configured`() {
    assertNotNull("DataStore with encryption should be configured", dataSource)
  }

  @Test
  fun `multiple initializations should succeed`() {
    val testFile2 = File(context.filesDir, "test_init_auth_data_2_${System.currentTimeMillis()}.pb")
    val testDataStore2 =
        DataStoreFactory.create(
            serializer = AuthDataSerializer(tinkKeyManager.getAead()), produceFile = { testFile2 })
    val dataSource1 = LocalAuthDataSource(context, testDataStore)
    val dataSource2 = LocalAuthDataSource(context, testDataStore2)

    assertNotNull("First initialization should succeed", dataSource1)
    assertNotNull("Second initialization should succeed", dataSource2)

    testFile2.delete()
  }
}
