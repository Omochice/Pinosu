package io.github.omochice.pinosu.feature.auth.domain.usecase

import android.util.Log
import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcher
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.feature.auth.data.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.model.error.StorageError
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
 * Implementation of [FetchRelayListUseCase]
 *
 * @param fetcher Fetcher for NIP-65 relay list metadata
 * @param authRepository Repository for caching relay list
 */
class FetchRelayListUseCaseImpl
@Inject
constructor(
    private val fetcher: Nip65RelayListFetcher,
    private val authRepository: AuthRepository,
) : FetchRelayListUseCase {

  @Suppress("ReturnCount")
  override suspend fun invoke(npubPubkey: String): Result<List<RelayConfig>> {
    val hexPubkey =
        Pubkey.parse(npubPubkey)?.hex
            ?: return Result.failure(
                IllegalArgumentException("Invalid npub format: must be Bech32-encoded (npub1...)"))

    val fetchResult = fetcher.fetchRelayList(hexPubkey)

    if (fetchResult.isFailure) {
      return fetchResult
    }

    val relays = fetchResult.getOrNull() ?: emptyList()

    try {
      authRepository.saveRelayList(relays)
    } catch (e: StorageError) {
      Log.w(TAG, "Failed to cache relay list: ${e.message}")
    }

    return Result.success(relays)
  }

  companion object {
    private const val TAG = "FetchRelayListUseCase"
  }
}
