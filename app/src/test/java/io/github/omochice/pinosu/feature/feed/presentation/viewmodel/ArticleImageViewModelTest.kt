package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.feature.feed.domain.repository.ArticleImageRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleImageViewModelTest {

  /** Repository whose lookups finish only when the test completes them */
  private class PendingRepository : ArticleImageRepository {
    val requested = mutableListOf<String>()
    val pending = mutableMapOf<String, CompletableDeferred<String?>>()
    var running = 0
    var maxRunning = 0

    override suspend fun findImage(link: String): String? {
      requested.add(link)
      running++
      maxRunning = maxOf(maxRunning, running)
      try {
        return pending.getOrPut(link) { CompletableDeferred() }.await()
      } finally {
        running--
      }
    }

    fun complete(link: String, image: String?) {
      pending.getOrPut(link) { CompletableDeferred() }.complete(image)
    }
  }

  private val repository = PendingRepository()
  private val viewModel = ArticleImageViewModel(repository)

  @Test
  fun `loading a link publishes the image of its article`() = runTest {
    repository.complete(LINK, IMAGE)

    viewModel.load(LINK)

    assertEquals(mapOf(LINK to IMAGE), viewModel.images.value)
  }

  @Test
  fun `a link is fetched only once however often it is loaded`() = runTest {
    repository.complete(LINK, IMAGE)

    viewModel.load(LINK)
    viewModel.load(LINK)

    assertEquals(listOf(LINK), repository.requested)
  }

  @Test
  fun `a link loaded again while its fetch runs is not fetched twice`() = runTest {
    launch { viewModel.load(LINK) }
    launch { viewModel.load(LINK) }
    runCurrent()
    repository.complete(LINK, IMAGE)
    runCurrent()

    assertEquals(listOf(LINK), repository.requested)
    assertEquals(mapOf(LINK to IMAGE), viewModel.images.value)
  }

  @Test
  fun `an article without an image is remembered so it is not fetched again`() = runTest {
    repository.complete(LINK, null)

    viewModel.load(LINK)
    viewModel.load(LINK)

    assertEquals(listOf(LINK), repository.requested)
    assertTrue(viewModel.images.value.containsKey(LINK), "the miss should be remembered")
    assertEquals(null, viewModel.images.value[LINK])
  }

  @Test
  fun `no more than the limit of fetches run at once`() = runTest {
    val links = (1..ArticleImageViewModel.MAX_CONCURRENT_FETCHES + 2).map { "$LINK/$it" }
    links.forEach { link -> launch { viewModel.load(link) } }
    runCurrent()

    assertEquals(ArticleImageViewModel.MAX_CONCURRENT_FETCHES, repository.running)

    repository.complete(links.first(), IMAGE)
    runCurrent()

    assertEquals(ArticleImageViewModel.MAX_CONCURRENT_FETCHES, repository.running)
    links.forEach { repository.complete(it, null) }
    runCurrent()

    assertEquals(links.toSet(), repository.requested.toSet())
    assertEquals(ArticleImageViewModel.MAX_CONCURRENT_FETCHES, repository.maxRunning)
  }

  @Test
  fun `a load cancelled before it finishes is fetched again by the next load`() = runTest {
    val first = launch { viewModel.load(LINK) }
    runCurrent()
    first.cancel()
    runCurrent()

    assertFalse(viewModel.images.value.containsKey(LINK), "nothing should be remembered")

    repository.complete(LINK, IMAGE)
    viewModel.load(LINK)

    assertEquals(listOf(LINK, LINK), repository.requested)
    assertEquals(mapOf(LINK to IMAGE), viewModel.images.value)
  }

  companion object {
    private const val LINK = "https://example.com/article"
    private const val IMAGE = "https://example.com/og.png"
  }
}
