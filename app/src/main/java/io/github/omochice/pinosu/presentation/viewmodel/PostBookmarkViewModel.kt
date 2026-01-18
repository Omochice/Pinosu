package io.github.omochice.pinosu.presentation.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.PostBookmarkUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * ViewModel for post bookmark screen
 *
 * Manages form state and NIP-55 signing flow for creating bookmarks.
 *
 * @property postBookmarkUseCase UseCase for posting bookmarks
 * @property getLoginStateUseCase UseCase for retrieving login state
 * @property nip55SignerClient Client for NIP-55 signer communication
 */
@HiltViewModel
class PostBookmarkViewModel
@Inject
constructor(
    private val postBookmarkUseCase: PostBookmarkUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val nip55SignerClient: Nip55SignerClient,
) : ViewModel() {

  companion object {
    private const val TAG = "PostBookmarkViewModel"
    private const val TYPE_SIGN_EVENT = "sign_event"
  }

  private val _uiState = MutableStateFlow(PostBookmarkUiState())
  val uiState: StateFlow<PostBookmarkUiState> = _uiState.asStateFlow()

  /** Update URL field */
  fun updateUrl(url: String) {
    _uiState.value = _uiState.value.copy(url = url, errorMessage = null)
  }

  /** Update title field */
  fun updateTitle(title: String) {
    _uiState.value = _uiState.value.copy(title = title)
  }

  /** Update categories field */
  fun updateCategories(categories: String) {
    _uiState.value = _uiState.value.copy(categories = categories)
  }

  /** Update comment field */
  fun updateComment(comment: String) {
    _uiState.value = _uiState.value.copy(comment = comment)
  }

  /** Dismiss error message */
  fun dismissError() {
    _uiState.value = _uiState.value.copy(errorMessage = null)
  }

  /** Prepare to submit bookmark by creating unsigned event and starting NIP-55 signing flow */
  fun prepareAndSignEvent() {
    val state = _uiState.value

    if (state.url.isBlank()) {
      _uiState.value = _uiState.value.copy(errorMessage = "URLを入力してください")
      return
    }

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

      val user = getLoginStateUseCase()
      if (user == null) {
        _uiState.value = _uiState.value.copy(errorMessage = "ログインが必要です", isSubmitting = false)
        return@launch
      }

      val hexPubkey = io.github.omochice.pinosu.data.util.Bech32.npubToHex(user.pubkey)
      if (hexPubkey == null) {
        _uiState.value = _uiState.value.copy(errorMessage = "公開鍵の変換に失敗しました", isSubmitting = false)
        return@launch
      }

      val categoryList = state.categories.split(",").map { it.trim() }.filter { it.isNotBlank() }

      val result =
          postBookmarkUseCase.createUnsignedEvent(
              pubkey = hexPubkey,
              url = state.url,
              title = state.title.takeIf { it.isNotBlank() },
              categories = categoryList,
              comment = state.comment)

      if (result.isSuccess) {
        val unsignedEvent = result.getOrNull()!!
        val eventJson = unsignedEvent.toJsonForSigning()
        _uiState.value =
            _uiState.value.copy(
                unsignedEventJson = eventJson,
                unsignedEvent = unsignedEvent,
                isSubmitting = false,
                readyToSign = true)
      } else {
        _uiState.value = _uiState.value.copy(errorMessage = "イベント作成に失敗しました", isSubmitting = false)
      }
    }
  }

  /**
   * Create Intent for NIP-55 event signing
   *
   * Must be called after prepareSignEventIntent() has set unsignedEventJson
   */
  fun createSignEventIntent(): Intent? {
    val eventJson = _uiState.value.unsignedEventJson
    if (eventJson == null) {
      _uiState.value = _uiState.value.copy(errorMessage = "イベントが作成されていません")
      return null
    }

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${Nip55SignerClient.NOSTRSIGNER_SCHEME}:"))
    intent.`package` = Nip55SignerClient.NIP55_SIGNER_PACKAGE_NAME
    intent.putExtra("type", TYPE_SIGN_EVENT)
    intent.putExtra("event", eventJson)
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    return intent
  }

  /**
   * Process signed event response from NIP-55 signer
   *
   * @param resultCode ActivityResult resultCode
   * @param data Intent data containing signature or signed event
   */
  fun processSignedEvent(resultCode: Int, data: Intent?) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

      if (resultCode == Activity.RESULT_CANCELED) {
        _uiState.value = _uiState.value.copy(errorMessage = "署名がキャンセルされました", isSubmitting = false)
        return@launch
      }

      if (data == null) {
        _uiState.value =
            _uiState.value.copy(errorMessage = "署名済みイベントの構築に失敗しました", isSubmitting = false)
        return@launch
      }

      val rejected = data.getBooleanExtra("rejected", false)
      if (rejected) {
        _uiState.value = _uiState.value.copy(errorMessage = "署名がキャンセルされました", isSubmitting = false)
        return@launch
      }

      try {
        val signedEventJson = extractSignedEventJson(data)
        if (signedEventJson == null) {
          _uiState.value =
              _uiState.value.copy(errorMessage = "NIP-55対応アプリからの応答が不正です", isSubmitting = false)
          return@launch
        }

        val publishResult = postBookmarkUseCase.publishSignedEvent(signedEventJson)
        if (publishResult.isSuccess) {
          _uiState.value =
              _uiState.value.copy(postSuccess = true, isSubmitting = false, errorMessage = null)
        } else {
          Log.e(TAG, "Failed to publish bookmark", publishResult.exceptionOrNull())
          _uiState.value =
              _uiState.value.copy(errorMessage = "ブックマークの投稿に失敗しました", isSubmitting = false)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error processing signed event", e)
        _uiState.value =
            _uiState.value.copy(errorMessage = "署名済みイベントの構築に失敗しました", isSubmitting = false)
      }
    }
  }

  /**
   * Extract signed event JSON from NIP-55 response
   *
   * Handles both complete signed event JSON and signature-only responses
   *
   * @param data Intent data from NIP-55 signer
   * @return Complete signed event JSON string, or null if extraction failed
   */
  private fun extractSignedEventJson(data: Intent): String? {
    val result = data.getStringExtra("result") ?: return null

    return try {
      val json = JSONObject(result)
      if (json.has("sig") && json.has("id")) {
        result
      } else {
        val signature = result
        val unsignedEvent = _uiState.value.unsignedEvent ?: return null
        unsignedEvent.toSignedEventJson(signature)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Result is not JSON, treating as signature", e)
      val signature = result
      val unsignedEvent = _uiState.value.unsignedEvent ?: return null
      unsignedEvent.toSignedEventJson(signature)
    }
  }
}

/**
 * Post bookmark screen UI state
 *
 * @property url Bookmark URL
 * @property title Bookmark title
 * @property categories Comma-separated categories
 * @property comment Bookmark comment
 * @property isSubmitting Whether currently submitting
 * @property errorMessage Error message
 * @property postSuccess Whether posting was successful
 * @property unsignedEventJson Unsigned event JSON for NIP-55 signing
 * @property unsignedEvent Unsigned event object for creating signed event
 * @property readyToSign Whether unsigned event is ready and NIP-55 intent should be launched
 */
data class PostBookmarkUiState(
    val url: String = "",
    val title: String = "",
    val categories: String = "",
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val postSuccess: Boolean = false,
    val unsignedEventJson: String? = null,
    val unsignedEvent: io.github.omochice.pinosu.data.model.UnsignedNostrEvent? = null,
    val readyToSign: Boolean = false,
)
