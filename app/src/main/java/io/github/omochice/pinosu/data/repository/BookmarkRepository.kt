package io.github.omochice.pinosu.data.repository

import io.github.omochice.pinosu.domain.model.BookmarkList

/** Repository interface for bookmark operations */
interface BookmarkRepository {

  /**
   * Fetch bookmark list (kind:10003) for a user
   *
   * @param pubkey User's public key
   * @return BookmarkList or null if not found
   */
  suspend fun getBookmarkList(pubkey: String): Result<BookmarkList?>
}
