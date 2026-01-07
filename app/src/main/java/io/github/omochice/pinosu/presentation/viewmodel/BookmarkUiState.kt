package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.BookmarkItem

data class BookmarkUiState(
    val isLoading: Boolean = false,
    val bookmarks: List<BookmarkItem> = emptyList(),
    val error: String? = null,
)
