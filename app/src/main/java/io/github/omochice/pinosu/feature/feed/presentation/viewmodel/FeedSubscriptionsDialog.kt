package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.feature.feed.domain.model.FeedCandidate

/** Question the user has to answer before a change to the subscription list continues */
sealed interface FeedSubscriptionsDialog {

  /**
   * The entered page advertises several feeds, so the user has to pick the one to add
   *
   * @property candidates Advertised feeds in page order
   */
  data class ChooseCandidate(val candidates: List<FeedCandidate>) : FeedSubscriptionsDialog

  /**
   * The user has never published a list, so they must agree that it will be public
   *
   * @property event The first list, signed only once the user agrees
   */
  data class ConfirmPublicList(val event: UnsignedNostrEvent) : FeedSubscriptionsDialog
}
