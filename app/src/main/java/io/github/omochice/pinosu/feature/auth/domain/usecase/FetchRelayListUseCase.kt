package io.github.omochice.pinosu.feature.auth.domain.usecase

import android.util.Log
import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcher
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.util.Bech32
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import javax.inject.Inject

/**
 * UseCase for fetching and caching NIP-65 relay list
 *
 * Orchestrates fetching relay list from Nostr relays and caching locally.
 */
interface FetchRelayListUseCase {

  /**
   * Fetch relay list for the given public key and cache it locally
   *
   * @param npubPubkey Bech32-encoded public key (npub1...)
   * @return Success(List<RelayConfig>) with fetched relays (may be empty), Failure if npub is
   *   invalid or fetch fails
   */
  suspend operator fun invoke(npubPubkey: String): Result<List<RelayConfig>>
}

/**
 * Implementation of FetchRelayListUseCase
 *
 * @property fetcher NIP-65 relay list fetcher
 * @property localAuthDataSource Local data source for caching relay list
 */
class FetchRelayListUseCaseImpl
@Inject
constructor(
    private val fetcher: Nip65RelayListFetcher,
    private val localAuthDataSource: LocalAuthDataSource,
) : FetchRelayListUseCase {

  companion object {
    private const val TAG = "FetchRelayListUseCase"
  }

  override suspend fun invoke(npubPubkey: String): Result<List<RelayConfig>> {
    if (!npubPubkey.startsWith("npub1") || npubPubkey.length != 63) {
      return Result.failure(
          IllegalArgumentException("Invalid npub format: must be Bech32-encoded (npub1...)"))
    }

    val hexPubkey =
        try {
          Bech32.npubToHex(npubPubkey)
        } catch (e: Exception) {
          null
        }
            ?: return Result.failure(
                IllegalArgumentException("Invalid npub format: must be Bech32-encoded (npub1...)"))

    val fetchResult = fetcher.fetchRelayList(hexPubkey)

    if (fetchResult.isFailure) {
      return fetchResult
    }

    val relays = fetchResult.getOrNull() ?: emptyList()

    try {
      localAuthDataSource.saveRelayList(relays)
    } catch (e: Exception) {
      Log.w(TAG, "Failed to cache relay list: ${e.message}")
    }

    return Result.success(relays)
  }
}
