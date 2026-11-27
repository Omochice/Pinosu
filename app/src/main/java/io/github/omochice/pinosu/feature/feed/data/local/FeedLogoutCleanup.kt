package io.github.omochice.pinosu.feature.feed.data.local

import android.util.Log
import io.github.omochice.pinosu.core.logout.LogoutCleanup
import io.github.omochice.pinosu.feature.feed.data.http.FeedHttp
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache

/**
 * Deletes the downloaded feeds and the article image lookups kept on disk
 *
 * @param feedHttpCache Cache the feed client keeps downloaded feeds in
 * @param articleImages Store of the article image lookups
 */
class FeedLogoutCleanup
@Inject
constructor(
    @FeedHttp private val feedHttpCache: Cache,
    private val articleImages: ArticleImageStore,
) : LogoutCleanup {

  override suspend fun cleanUp() {
    try {
      withContext(Dispatchers.IO) { feedHttpCache.evictAll() }
    } catch (e: IOException) {
      Log.w(TAG, "Failed to delete the downloaded feeds", e)
    }
    try {
      articleImages.clear()
    } catch (e: IOException) {
      Log.w(TAG, "Failed to delete the stored article images", e)
    }
  }

  private companion object {
    const val TAG = "FeedLogoutCleanup"
  }
}
