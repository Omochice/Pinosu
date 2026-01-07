package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.domain.model.BookmarkList

interface GetBookmarkListUseCase {
  suspend operator fun invoke(pubkey: String): Result<BookmarkList?>
}
