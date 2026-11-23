package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.core.nip.nip55.Nip55Error
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.model.error.HttpFeedNotSupportedException
import io.github.omochice.pinosu.feature.feed.domain.model.error.NoFeedFoundException
import io.github.omochice.pinosu.feature.feed.domain.model.error.ReadOnlyLoginException

/** Outcome of a change to the subscription list that the user is told about */
enum class FeedSubscriptionsMessage {
  /** The feed to add is already subscribed */
  AlreadySubscribed,

  /** The list was published, but some relays did not accept it */
  PartiallyPublished,

  /** The user logged in read-only and cannot change the list */
  ReadOnly,

  /** The user has no relays of their own to load the current list from */
  NoUserRelays,

  /** Every relay refused to serve the current list */
  AccessDenied,

  /** No relay answered for the current list */
  Unreachable,

  /** The entered URL neither is a feed nor advertises one */
  NoFeedFound,

  /** The entered feed is served over plain HTTP only */
  HttpFeedNotSupported,

  /** The signer did not sign the list */
  SigningCancelled,

  /** The signer's answer could not be turned into a signed list */
  SignedEventBuildFailed,

  /** The signed list could not be published */
  PublishFailed,

  /** Preparing the change failed for any other reason */
  ChangeFailed,
}

/**
 * Message for a failure to prepare a change
 *
 * @param error Failure reported while preparing the change
 * @return The message to show
 */
internal fun prepareFailureMessage(error: Throwable): FeedSubscriptionsMessage =
    when (error) {
      is ReadOnlyLoginException -> FeedSubscriptionsMessage.ReadOnly
      is FeedSubscriptionListUnavailableException -> unavailableListMessage(feedLoadErrorOf(error))
      is NoFeedFoundException -> FeedSubscriptionsMessage.NoFeedFound
      is HttpFeedNotSupportedException -> FeedSubscriptionsMessage.HttpFeedNotSupported
      else -> FeedSubscriptionsMessage.ChangeFailed
    }

/**
 * Message for a failure to sign or publish a prepared list
 *
 * @param error Failure reported while signing or publishing
 * @return The message to show
 */
internal fun publishFailureMessage(error: Throwable): FeedSubscriptionsMessage =
    when (error) {
      is Nip55Error -> FeedSubscriptionsMessage.SigningCancelled
      is UnusableSignatureException -> FeedSubscriptionsMessage.SignedEventBuildFailed
      else -> FeedSubscriptionsMessage.PublishFailed
    }

private fun unavailableListMessage(reason: FeedLoadError): FeedSubscriptionsMessage =
    when (reason) {
      FeedLoadError.NoUserRelays -> FeedSubscriptionsMessage.NoUserRelays
      FeedLoadError.AccessDenied -> FeedSubscriptionsMessage.AccessDenied
      FeedLoadError.Unreachable -> FeedSubscriptionsMessage.Unreachable
      FeedLoadError.Unknown -> FeedSubscriptionsMessage.ChangeFailed
    }
