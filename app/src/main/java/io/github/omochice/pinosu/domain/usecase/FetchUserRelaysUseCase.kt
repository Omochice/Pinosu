package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.relay.RelayConfig
import io.github.omochice.pinosu.data.repository.RelayListRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for fetching user's relay list via NIP-65
 *
 * Fetches the user's preferred relays from kind:10002 events and caches them locally.
 */
interface FetchUserRelaysUseCase {

  /**
   * Fetch user's relay list from NIP-65 relay list metadata
   *
   * @param pubkey User's public key in Bech32 npub format
   * @return Success with list of relay configurations, or Failure on error
   */
  suspend operator fun invoke(pubkey: String): Result<List<RelayConfig>>
}

/**
 * Implementation of FetchUserRelaysUseCase
 *
 * @property relayListRepository Repository for fetching relay lists
 */
@Singleton
class FetchUserRelaysUseCaseImpl
@Inject
constructor(private val relayListRepository: RelayListRepository) : FetchUserRelaysUseCase {

  override suspend fun invoke(pubkey: String): Result<List<RelayConfig>> {
    return relayListRepository.fetchAndCacheUserRelays(pubkey)
  }
}
