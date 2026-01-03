package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.BookmarkRepository
import io.github.omochice.pinosu.domain.model.BookmarkList
import javax.inject.Inject

/**
 * Implementation of GetBookmarkListUseCase
 *
 * @property bookmarkRepository Repository for bookmark operations
 */
class GetBookmarkListUseCaseImpl
@Inject
constructor(private val bookmarkRepository: BookmarkRepository) : GetBookmarkListUseCase {

  override suspend fun invoke(pubkey: String): Result<BookmarkList?> {
    return bookmarkRepository.getBookmarkList(pubkey)
  }
}
