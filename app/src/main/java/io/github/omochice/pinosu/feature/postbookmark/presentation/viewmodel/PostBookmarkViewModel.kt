package io.github.omochice.pinosu.feature.postbookmark.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.core.nip.nip55.SignedEventResponse
import io.github.omochice.pinosu.feature.postbookmark.domain.usecase.PostBookmarkUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for post bookmark screen
 *
 * Manages form state and handles bookmark posting flow including NIP-55 signing.
 *
 * @param postBookmarkUseCase UseCase for creating and publishing bookmark events
 * @param nip55SignerClient Client for NIP-55 signer interaction
 */
@HiltViewModel
class PostBookmarkViewModel
@Inject
constructor(
    private val postBookmarkUseCase: PostBookmarkUseCase,
    private val nip55SignerClient: Nip55SignerClient
) : ViewModel() {

  private val _uiState = MutableStateFlow(PostBookmarkUiState())
  val uiState: StateFlow<PostBookmarkUiState> = _uiState.asStateFlow()

  private var pendingUnsignedEvent: UnsignedNostrEvent? = null

  /**
   * Update URL field
   *
   * Strips http:// or https:// prefix if present.
   *
   * @param input Raw URL input from user
   */
  fun updateUrl(input: String) {
    if (_uiState.value.isEditMode) return
    val strippedUrl = stripUrlScheme(input)
    _uiState.update { it.copy(url = strippedUrl) }
  }

  /**
   * Update title field
   *
   * @param title New title value
   */
  fun updateTitle(title: String) {
    _uiState.update { it.copy(title = title) }
  }

  /**
   * Update categories field
   *
   * @param categories New categories value (comma-separated)
   */
  fun updateCategories(categories: String) {
    _uiState.update { it.copy(categories = categories) }
  }

  /**
   * Update comment field
   *
   * @param comment New comment value
   */
  fun updateComment(comment: String) {
    _uiState.update { it.copy(comment = comment) }
  }

  /**
   * Initialize the form for editing an existing bookmark
   *
   * @param url URL without scheme (read-only in edit mode)
   * @param title Existing bookmark title
   * @param categories Existing comma-separated categories
   * @param comment Existing bookmark comment
   */
  fun initializeForEdit(url: String, title: String, categories: String, comment: String) {
    _uiState.update {
      it.copy(
          url = url, title = title, categories = categories, comment = comment, isEditMode = true)
    }
  }

  /** Dismiss error message */
  fun dismissError() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  /** Reset post success state */
  fun resetPostSuccess() {
    _uiState.update { it.copy(postSuccess = false) }
  }

  /**
   * Prepare sign event Intent synchronously (for launcher)
   *
   * @param onReady Callback with Intent when ready, or null on failure
   */
  fun prepareSignEventIntent(onReady: (Intent?) -> Unit) {
    val state = _uiState.value

    if (state.url.isBlank()) {
      _uiState.update { it.copy(errorMessage = "URLを入力してください") }
      onReady(null)
      return
    }

    _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

    viewModelScope.launch {
      val categories = state.categories.split(",").map { it.trim() }.filter { it.isNotBlank() }

      postBookmarkUseCase
          .createUnsignedEvent(
              url = state.url,
              title = state.title,
              categories = categories,
              comment = state.comment)
          .onSuccess { unsignedEvent ->
            pendingUnsignedEvent = unsignedEvent
            val eventJson = unsignedEvent.toJson()
            val intent = nip55SignerClient.createSignEventIntent(eventJson)
            onReady(intent)
          }
          .onFailure { error ->
            _uiState.update {
              it.copy(isSubmitting = false, errorMessage = error.message ?: "イベント作成に失敗しました")
            }
            onReady(null)
          }
    }
  }

  /**
   * Process signed event response from NIP-55 signer
   *
   * NIP-55 signer may return either:
   * - Complete signed event JSON (if returnType=event works)
   * - Signature only (hex string)
   *
   * This method handles both cases.
   *
   * @param resultCode Activity result code
   * @param data Intent data from signer
   */
  fun processSignedEvent(resultCode: Int, data: Intent?) {
    nip55SignerClient
        .handleSignEventResponse(resultCode, data)
        .onSuccess { response -> viewModelScope.launch { performPublishSignedEvent(response) } }
        .onFailure { error ->
          _uiState.update {
            it.copy(isSubmitting = false, errorMessage = error.message ?: "署名がキャンセルされました")
          }
        }
  }

  private suspend fun performPublishSignedEvent(response: SignedEventResponse) {
    val signedEventJson =
        UnsignedNostrEvent.buildSignedEventJson(response.signedEventJson, pendingUnsignedEvent)
            ?: run {
              _uiState.update { it.copy(isSubmitting = false, errorMessage = "署名済みイベントの構築に失敗しました") }
              return
            }

    postBookmarkUseCase
        .publishSignedEvent(signedEventJson)
        .onSuccess { _uiState.update { it.copy(isSubmitting = false, postSuccess = true) } }
        .onFailure { error ->
          _uiState.update {
            it.copy(isSubmitting = false, errorMessage = error.message ?: "ブックマークの投稿に失敗しました")
          }
        }
  }
}

private fun stripUrlScheme(url: String): String {
  return when {
    url.startsWith("https://", ignoreCase = true) -> url.substring("https://".length)
    url.startsWith("http://", ignoreCase = true) -> url.substring("http://".length)
    else -> url
  }
}
