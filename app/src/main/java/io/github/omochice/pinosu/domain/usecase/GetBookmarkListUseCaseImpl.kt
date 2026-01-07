package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.BookmarkRepository
import io.github.omochice.pinosu.domain.model.BookmarkList
import javax.inject.Inject

class GetBookmarkListUseCaseImpl
@Inject
constructor(private val bookmarkRepository: BookmarkRepository) : GetBookmarkListUseCase {

  override suspend fun invoke(pubkey: String): Result<BookmarkList?> {
    return bookmarkRepository.getBookmarkList(pubkey)
  }
}
