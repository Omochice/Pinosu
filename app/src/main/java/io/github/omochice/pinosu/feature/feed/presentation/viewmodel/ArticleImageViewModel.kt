package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.feature.feed.domain.repository.ArticleImageRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * ViewModel holding the article images of the timeline items whose feed gives them none
 *
 * Each article is looked up at most once while the timeline lives, including when it has no image,
 * so scrolling back to a card does not fetch its page again.
 *
 * @param repository Repository that finds the image of an article
 */
@HiltViewModel
class ArticleImageViewModel @Inject constructor(private val repository: ArticleImageRepository) :
    ViewModel() {

  private val _images = MutableStateFlow<Map<String, String?>>(emptyMap())

  /** Image of each article link looked up so far, null for an article without one */
  val images: StateFlow<Map<String, String?>> = _images.asStateFlow()

  private val inFlight: MutableSet<String> = ConcurrentHashMap.newKeySet()

  private val permits = Semaphore(MAX_CONCURRENT_FETCHES)

  /**
   * Look up the image of an article unless it is already known or being looked up
   *
   * This runs in the caller's coroutine rather than in a ViewModel scope, so that a card scrolled
   * away before its turn cancels its lookup instead of delaying the cards still on screen. A
   * cancelled lookup is not remembered and runs again on the next call.
   *
   * @param link Absolute URL of the article page
   */
  suspend fun load(link: String) {
    if (link in _images.value || !inFlight.add(link)) return
    try {
      val image = permits.withPermit { repository.findImage(link) }
      _images.update { it + (link to image) }
    } finally {
      inFlight.remove(link)
    }
  }

  companion object {
    /** Most article pages fetched at the same time */
    const val MAX_CONCURRENT_FETCHES = 4
  }
}
