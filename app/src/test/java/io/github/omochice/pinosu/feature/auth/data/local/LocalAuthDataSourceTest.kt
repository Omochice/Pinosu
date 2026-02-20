package io.github.omochice.pinosu.feature.auth.data.local

import androidx.datastore.core.DataStore
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [LocalAuthDataSource]
 *
 * Tests relay list caching functionality including save, retrieve, and clear operations. Uses mock
 * DataStore to test data operations without encryption.
 */
class LocalAuthDataSourceTest {

  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var testDataStore: DataStore<AuthData>
  private lateinit var dataFlow: MutableStateFlow<AuthData>

  @Before
  fun setup() {
    dataFlow = MutableStateFlow(AuthData.DEFAULT)

    testDataStore = mockk {
      every { data } returns dataFlow
      coEvery { updateData(any()) } coAnswers
          {
            val transform = firstArg<suspend (AuthData) -> AuthData>()
            val newValue = transform(dataFlow.value)
            dataFlow.value = newValue
            newValue
          }
    }

    val productionDataStore = mockk<DataStore<AuthData>>()
    localAuthDataSource = LocalAuthDataSource(productionDataStore)
    localAuthDataSource.setTestDataStore(testDataStore)
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
    val relays =
        listOf(
            RelayConfig(url = "wss://relay1.example.com", read = true, write = true),
            RelayConfig(url = "wss://relay2.example.com", read = true, write = false))
    dataFlow.value = AuthData(relayList = relays)

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
    dataFlow.value =
        AuthData(relayList = listOf(RelayConfig(url = "test", read = true, write = true)))

    localAuthDataSource.clearLoginState()

    assertNull("relay_list should be cleared", dataFlow.value.relayList)
  }

  @Test
  fun `saveUser with ReadOnly loginMode should persist read_only`() = runTest {
    val pubkey = io.github.omochice.pinosu.core.model.Pubkey.parse("npub1" + "a".repeat(59))!!
    val user = io.github.omochice.pinosu.feature.auth.domain.model.User(pubkey)

    localAuthDataSource.saveUser(user, LoginMode.ReadOnly)

    assertEquals(LoginMode.ReadOnly, dataFlow.value.loginMode)
  }

  @Test
  fun `saveUser with Nip55Signer loginMode should persist nip55_signer`() = runTest {
    val pubkey = io.github.omochice.pinosu.core.model.Pubkey.parse("npub1" + "a".repeat(59))!!
    val user = io.github.omochice.pinosu.feature.auth.domain.model.User(pubkey)

    localAuthDataSource.saveUser(user, LoginMode.Nip55Signer)

    assertEquals(LoginMode.Nip55Signer, dataFlow.value.loginMode)
  }

  @Test
  fun `getLoginMode should return stored login mode`() = runTest {
    dataFlow.value = AuthData(userPubkey = "npub1test", loginMode = LoginMode.ReadOnly)

    val result = localAuthDataSource.getLoginMode()

    assertEquals(LoginMode.ReadOnly, result)
  }

  @Test
  fun `getLoginMode when default should return Nip55Signer`() = runTest {
    dataFlow.value = AuthData(userPubkey = "npub1test")

    val result = localAuthDataSource.getLoginMode()

    assertEquals(LoginMode.Nip55Signer, result)
  }
}
