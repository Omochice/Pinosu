package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.domain.model.BookmarkList

/**
 * UseCase interface for retrieving bookmark list
 *
 * Fetches kind:39701 bookmark events from Nostr relays for a given user.
 */
interface GetBookmarkListUseCase {
  /**
   * Retrieve bookmark list for the specified public key
   *
   * @param pubkey Nostr public key (Bech32-encoded format, starts with npub1)
   * @return Success(BookmarkList) if found, Success(null) if no bookmarks, Failure on error
   */
  suspend operator fun invoke(pubkey: String): Result<BookmarkList?>
}
