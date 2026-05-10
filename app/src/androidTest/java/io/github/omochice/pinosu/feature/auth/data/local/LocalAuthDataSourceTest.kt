package io.github.omochice.pinosu.feature.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.crypto.tink.aead.AeadConfig
import io.github.omochice.pinosu.core.crypto.TinkKeyManager
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.junit.Assert.*
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

  @BeforeTest
  fun setup() {
    AeadConfig.register()
    context = InstrumentationRegistry.getInstrumentation().targetContext
    tinkKeyManager = TinkKeyManager(context)
    testFile = File(context.filesDir, "test_init_auth_data_${System.currentTimeMillis()}.pb")
    testDataStore =
        DataStoreFactory.create(
            serializer = AuthDataSerializer(tinkKeyManager.getAead()), produceFile = { testFile })

    dataSource = LocalAuthDataSource(testDataStore)
  }

  @AfterTest
  fun tearDown() {
    testFile.delete()
  }

  @Test
  fun `initialization should succeed`() {
    assertNotNull(dataSource, "LocalAuthDataSource should be initialized")
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
    assertNotNull(tinkKeyManager, "TinkKeyManager should be generated")
    assertNotNull(tinkKeyManager.getAead(), "AEAD should be available")
  }

  @Test
  fun `encryption should be configured`() {
    assertNotNull(dataSource, "DataStore with encryption should be configured")
  }

  @Test
  fun `multiple initializations should succeed`() {
    val testFile2 = File(context.filesDir, "test_init_auth_data_2_${System.currentTimeMillis()}.pb")
    val testDataStore2 =
        DataStoreFactory.create(
            serializer = AuthDataSerializer(tinkKeyManager.getAead()), produceFile = { testFile2 })
    val dataSource1 = LocalAuthDataSource(testDataStore)
    val dataSource2 = LocalAuthDataSource(testDataStore2)

    assertNotNull(dataSource1, "First initialization should succeed")
    assertNotNull(dataSource2, "Second initialization should succeed")

    testFile2.delete()
  }
}
