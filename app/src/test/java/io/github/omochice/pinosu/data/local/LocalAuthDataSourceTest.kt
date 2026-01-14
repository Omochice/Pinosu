package io.github.omochice.pinosu.data.local

import android.content.Context
import io.github.omochice.pinosu.data.relay.RelayConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Unit tests for [LocalAuthDataSource]
 *
 * Tests relay list caching functionality including save, retrieve, and clear operations. Uses real
 * SharedPreferences through Robolectric to test actual JSON serialization behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LocalAuthDataSourceTest {

  private lateinit var sharedPreferences: android.content.SharedPreferences
  private lateinit var localAuthDataSource: LocalAuthDataSource

  @Before
  fun setup() {
    val context = RuntimeEnvironment.getApplication()
    sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().clear().commit()

    localAuthDataSource = LocalAuthDataSource(context)
    localAuthDataSource.setTestSharedPreferences(sharedPreferences)
  }

  @Test
  fun `saveAndGetRelayList round trip should preserve data`() = runTest {
    val relays =
        listOf(
            RelayConfig(url = "wss://relay1.example.com", read = true, write = true),
            RelayConfig(url = "wss://relay2.example.com", read = true, write = false))

    localAuthDataSource.saveRelayList(relays)
    val result = localAuthDataSource.getRelayList()

    assertNotNull("Expected relay_list to be retrieved", result)
    assertEquals(2, result?.size)
    assertEquals("wss://relay1.example.com", result?.get(0)?.url)
    assertEquals(true, result?.get(0)?.read)
    assertEquals(true, result?.get(0)?.write)
    assertEquals("wss://relay2.example.com", result?.get(1)?.url)
    assertEquals(true, result?.get(1)?.read)
    assertEquals(false, result?.get(1)?.write)
  }

  @Test
  fun `getRelayList when saved should return relays`() = runTest {
    val jsonString =
        """[{"url":"wss://relay1.example.com","read":true,"write":true},{"url":"wss://relay2.example.com","read":true,"write":false}]"""
    sharedPreferences.edit().putString("relay_list", jsonString).commit()

    val result = localAuthDataSource.getRelayList()

    assertEquals(2, result?.size)
    assertEquals("wss://relay1.example.com", result?.get(0)?.url)
    assertEquals(true, result?.get(0)?.read)
    assertEquals(true, result?.get(0)?.write)
    assertEquals("wss://relay2.example.com", result?.get(1)?.url)
    assertEquals(true, result?.get(1)?.read)
    assertEquals(false, result?.get(1)?.write)
  }

  @Test
  fun `getRelayList when not set should return null`() = runTest {
    val result = localAuthDataSource.getRelayList()

    assertNull("Should return null when relay list is not set", result)
  }

  @Test
  fun `clearLoginState should clear relay list`() = runTest {
    sharedPreferences.edit().putString("relay_list", "test").commit()

    localAuthDataSource.clearLoginState()

    assertNull("relay_list should be cleared", sharedPreferences.getString("relay_list", null))
  }
}
