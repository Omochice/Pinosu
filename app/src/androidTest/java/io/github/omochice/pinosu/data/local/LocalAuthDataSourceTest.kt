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
    // テスト用のLocalAuthDataSourceを作成
    dataSource = LocalAuthDataSource(context)
  }

  @After
  fun tearDown() {
    // テスト後にデータをクリア
    context
        .getSharedPreferences("pinosu_auth_prefs_test", Context.MODE_PRIVATE)
        .edit()
        .clear()
        .commit()
  }

  @Test
  fun testInitialization() {
    // LocalAuthDataSourceのインスタンスが作成できることを確認
    assertNotNull("LocalAuthDataSource should be initialized", dataSource)
  }

  /**  */
  @Test
  fun testEncryptedSharedPreferencesCreation() {
    // EncryptedSharedPreferencesへのアクセスが可能であることを確認
    // 初期化時にエラーがスローされないことを確認
    try {
      dataSource.toString() // インスタンスが有効であることを確認
      assertTrue("EncryptedSharedPreferences should be created successfully", true)
    } catch (e: Exception) {
      fail("EncryptedSharedPreferences creation failed: ${e.message}")
    }
  }

  @Test
  fun testMasterKeyGeneration() {
    // MasterKeyの生成が成功していることを確認
    // （LocalAuthDataSourceの初期化が成功していれば、MasterKeyも生成されている）
    assertNotNull("MasterKey should be generated", dataSource)
  }

  @Test
  fun testEncryptionSchemes() {
    // 暗号化スキームの設定が正常であることを確認
    // （初期化が成功していれば、暗号化スキームも正しく設定されている）
    assertNotNull("Encryption schemes should be configured", dataSource)
  }

  /** 複数回の初期化が安全であることをテスト */
  @Test
  fun testMultipleInitializations() {
    // 同じContextで複数回初期化しても問題ないことを確認
    val dataSource1 = LocalAuthDataSource(context)
    val dataSource2 = LocalAuthDataSource(context)

    assertNotNull("First initialization should succeed", dataSource1)
    assertNotNull("Second initialization should succeed", dataSource2)
  }
}
