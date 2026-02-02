package io.github.omochice.pinosu.feature.comment.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.feature.comment.domain.usecase.GetCommentsForBookmarkUseCase
import io.github.omochice.pinosu.feature.comment.domain.usecase.PostCommentUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for bookmark detail screen
 *
 * Manages comment loading and posting flow including NIP-55 signing. Uses
 * [GetCommentsForBookmarkUseCase] to fetch comments, [PostCommentUseCase] to post comments, and
 * [Nip55SignerClient] for signing operations.
 */
@HiltViewModel
class BookmarkDetailViewModel
@Inject
constructor(
    private val getCommentsUseCase: GetCommentsForBookmarkUseCase,
    private val postCommentUseCase: PostCommentUseCase,
    private val nip55SignerClient: Nip55SignerClient,
) : ViewModel() {

  private val _uiState = MutableStateFlow(BookmarkDetailUiState())
  val uiState: StateFlow<BookmarkDetailUiState> = _uiState.asStateFlow()

  private var pendingUnsignedEvent: UnsignedNostrEvent? = null

  /**
   * Load comments for a bookmark event
   *
   * @param rootPubkey Hex-encoded public key of the bookmark author
   * @param dTag The d-tag of the bookmark event
   * @param rootEventId The event ID of the bookmark event
   * @param authorContent The bookmark event's content field
   * @param authorCreatedAt The bookmark event's created_at timestamp
   */
  fun loadComments(
      rootPubkey: String,
      dTag: String,
      rootEventId: String,
      authorContent: String,
      authorCreatedAt: Long,
  ) {
    _uiState.update { it.copy(isLoading = true, error = null) }

    viewModelScope.launch {
      getCommentsUseCase(
              rootPubkey = rootPubkey,
              dTag = dTag,
              rootEventId = rootEventId,
              authorContent = authorContent,
              authorCreatedAt = authorCreatedAt)
          .onSuccess { comments ->
            _uiState.update { it.copy(isLoading = false, comments = comments) }
          }
          .onFailure { error ->
            _uiState.update {
              it.copy(isLoading = false, error = error.message ?: "Failed to load comments")
            }
          }
    }
  }

  /**
   * Update comment input text
   *
   * @param input New comment input value
   */
  fun updateCommentInput(input: String) {
    _uiState.update { it.copy(commentInput = input) }
  }

  /**
   * Prepare sign comment Intent for NIP-55 signer
   *
   * @param rootPubkey Hex-encoded public key of the bookmark author
   * @param dTag The d-tag of the bookmark event
   * @param rootEventId The event ID of the bookmark event
   * @param onReady Callback with Intent when ready, or null on failure
   */
  fun prepareSignCommentIntent(
      rootPubkey: String,
      dTag: String,
      rootEventId: String,
      onReady: (Intent?) -> Unit,
  ) {
    val content = _uiState.value.commentInput

    if (content.isBlank()) {
      _uiState.update { it.copy(error = "コメントを入力してください") }
      onReady(null)
      return
    }

    _uiState.update { it.copy(isSubmitting = true, error = null) }

    viewModelScope.launch {
      postCommentUseCase
          .createUnsignedEvent(
              content = content, rootPubkey = rootPubkey, dTag = dTag, rootEventId = rootEventId)
          .onSuccess { unsignedEvent ->
            pendingUnsignedEvent = unsignedEvent
            val eventJson = unsignedEvent.toJson()
            val intent = nip55SignerClient.createSignEventIntent(eventJson)
            onReady(intent)
          }
          .onFailure { error ->
            _uiState.update {
              it.copy(isSubmitting = false, error = error.message ?: "コメント作成に失敗しました")
            }
            onReady(null)
          }
    }
  }

  /**
   * Process signed comment response from NIP-55 signer
   *
   * @param resultCode Activity result code
   * @param data Intent data from signer
   */
  fun processSignedComment(resultCode: Int, data: Intent?) {
    nip55SignerClient
        .handleSignEventResponse(resultCode, data)
        .onSuccess { response ->
          viewModelScope.launch {
            val signedEventJson = buildSignedEventJson(response.signedEventJson)
            if (signedEventJson == null) {
              _uiState.update { it.copy(isSubmitting = false, error = "署名済みイベントの構築に失敗しました") }
            } else {
              postCommentUseCase
                  .publishSignedEvent(signedEventJson)
                  .onSuccess {
                    _uiState.update {
                      it.copy(isSubmitting = false, postSuccess = true, commentInput = "")
                    }
                  }
                  .onFailure { error ->
                    _uiState.update {
                      it.copy(isSubmitting = false, error = error.message ?: "コメントの投稿に失敗しました")
                    }
                  }
            }
          }
        }
        .onFailure { error ->
          _uiState.update {
            it.copy(isSubmitting = false, error = error.message ?: "署名がキャンセルされました")
          }
        }
  }

  /** Reset post success state */
  fun resetPostSuccess() {
    _uiState.update { it.copy(postSuccess = false) }
  }

  /** Dismiss error message */
  fun dismissError() {
    _uiState.update { it.copy(error = null) }
  }

  private fun buildSignedEventJson(signerResponse: String): String? {
    if (signerResponse.startsWith("{")) {
      return signerResponse
    }
    val unsignedEvent = pendingUnsignedEvent ?: return null
    return unsignedEvent.toSignedJson(signerResponse)
  }
}
