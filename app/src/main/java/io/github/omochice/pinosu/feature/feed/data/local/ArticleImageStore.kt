package io.github.omochice.pinosu.feature.feed.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Article image lookups kept on disk, limited to the [MAX_LOOKUPS] most recent ones
 *
 * A lookup that cannot be read or written is treated as never stored, because the article page can
 * always be looked up again.
 */
class ArticleImageStore @Inject constructor(private val dataStore: DataStore<ArticleImageLookups>) {

  /** @return The stored lookup of the article at [link], or null when none is stored */
  suspend fun find(link: String): ArticleImageLookup? =
      try {
        dataStore.data.first().lookups[link]
      } catch (e: IOException) {
        Log.w(TAG, "Failed to read stored article images", e)
        null
      }

  /** Store the lookup of the article at [link], forgetting the oldest beyond [MAX_LOOKUPS] */
  suspend fun save(link: String, lookup: ArticleImageLookup) {
    try {
      dataStore.updateData { current ->
        ArticleImageLookups(newest(current.lookups + (link to lookup)))
      }
    } catch (e: IOException) {
      Log.w(TAG, "Failed to store the image of an article", e)
    }
  }

  /**
   * Forget every stored lookup
   *
   * @throws IOException when the store cannot be written
   */
  suspend fun clear() {
    dataStore.updateData { ArticleImageLookups() }
  }

  private fun newest(lookups: Map<String, ArticleImageLookup>): Map<String, ArticleImageLookup> =
      if (lookups.size <= MAX_LOOKUPS) {
        lookups
      } else {
        lookups.entries
            .sortedByDescending { it.value.lookedUpAtEpochMillis }
            .take(MAX_LOOKUPS)
            .associate { it.key to it.value }
      }

  companion object {
    private const val TAG = "ArticleImageStore"

    /** Most lookups kept */
    const val MAX_LOOKUPS = 500
  }
}
