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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

/**
 * ViewModel holding the article images of the timeline items whose feed gives them none
 *
 * Each article is looked up at most once while the timeline lives, including when it has no image,
 * so scrolling back to a card does not fetch its page again.
 */
@HiltViewModel
class ArticleImageViewModel @Inject constructor(private val repository: ArticleImageRepository) :
    ViewModel() {

  private val _images = MutableStateFlow<Map<String, String?>>(emptyMap())

  /** Image of each article link looked up so far, null for an article without one */
  val images: StateFlow<Map<String, String?>> = _images.asStateFlow()

  private val locks = ConcurrentHashMap<String, Mutex>()

  private val permits = Semaphore(MAX_CONCURRENT_FETCHES)

  /**
   * Look up the image of an article unless it is already known
   *
   * This runs in the caller's coroutine rather than in a ViewModel scope, so that a card scrolled
   * away before its turn cancels its lookup instead of delaying the cards still on screen.
   *
   * A call for a link already being looked up waits for that lookup rather than returning at once,
   * because the same item can be shown on two pager pages and the lookup of the page swiped away is
   * cancelled, which would otherwise leave the card on the remaining page without its image.
   *
   * @param link Absolute URL of the article page
   */
  suspend fun load(link: String) {
    if (link in _images.value) return
    locks
        .computeIfAbsent(link) { Mutex() }
        .withLock {
          if (link in _images.value) return
          val image = permits.withPermit { repository.findImage(link) }
          _images.update { it + (link to image) }
        }
  }

  companion object {
    /** Most article pages fetched at the same time */
    const val MAX_CONCURRENT_FETCHES = 4
  }
}
