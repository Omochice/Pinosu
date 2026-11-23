package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.feed.domain.usecase.PublishFeedSubscriptionListUseCase
import javax.inject.Inject

/**
 * Has a prepared subscription list signed by the NIP-55 signer and publishes the signed list
 *
 * The list awaiting a signature is kept in [SavedStateHandle] because the app process may die while
 * the external signer is in the foreground.
 *
 * @param nip55SignerClient Client for NIP-55 signer interaction
 * @param publishList UseCase that publishes the signed list
 * @param savedStateHandle Handle of the ViewModel that owns this publisher
 */
class FeedSubscriptionPublisher
@Inject
constructor(
    private val nip55SignerClient: Nip55SignerClient,
    private val publishList: PublishFeedSubscriptionListUseCase,
    private val savedStateHandle: SavedStateHandle,
) {

  /**
   * Remember [event] as the list awaiting a signature and build the signer request for it
   *
   * @param event Unsigned list event
   * @return Intent that opens the signer
   */
  fun signRequestFor(event: UnsignedNostrEvent): Intent {
    val eventJson = event.toJson()
    savedStateHandle[KEY_PENDING_EVENT] = eventJson
    return nip55SignerClient.createSignEventIntent(eventJson)
  }

  /**
   * Publish the list the signer answered for
   *
   * @param resultCode Activity result code from the signer
   * @param data Intent data from the signer
   * @return Which relays accepted the list, or a failure with
   *   [io.github.omochice.pinosu.core.nip.nip55.Nip55Error] when the signer did not sign, with
   *   [UnusableSignatureException] when the signature cannot be attached, or with the publishing
   *   failure
   */
  suspend fun publishSigned(resultCode: Int, data: Intent?): Result<PublishResult> =
      signedEventJson(resultCode, data)
          .fold(
              onSuccess = { json ->
                publishList(json).onSuccess { savedStateHandle.remove<String>(KEY_PENDING_EVENT) }
              },
              onFailure = { Result.failure(it) })

  private fun signedEventJson(resultCode: Int, data: Intent?): Result<String> =
      nip55SignerClient
          .handleSignEventResponse(resultCode, data)
          .fold(
              onSuccess = { response ->
                val pending =
                    savedStateHandle.get<String>(KEY_PENDING_EVENT)?.let {
                      UnsignedNostrEvent.fromJson(it)
                    }
                UnsignedNostrEvent.buildSignedEventJson(response.signedEventJson, pending)?.let {
                  Result.success(it)
                } ?: Result.failure(UnusableSignatureException())
              },
              onFailure = { Result.failure(it) })

  private companion object {
    const val KEY_PENDING_EVENT = "pending_feed_subscription_list_json"
  }
}
