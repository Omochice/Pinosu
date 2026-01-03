package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.domain.model.BookmarkList

/** UseCase interface for fetching bookmark list */
interface GetBookmarkListUseCase {

  /**
   * Fetch bookmark list for a user
   *
   * @param pubkey User's public key
   * @return BookmarkList or null if not found
   */
  suspend operator fun invoke(pubkey: String): Result<BookmarkList?>
}
