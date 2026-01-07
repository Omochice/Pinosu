package io.github.omochice.pinosu.data.repository

import io.github.omochice.pinosu.domain.model.BookmarkList

interface BookmarkRepository {
  suspend fun getBookmarkList(pubkey: String): Result<BookmarkList?>
}
