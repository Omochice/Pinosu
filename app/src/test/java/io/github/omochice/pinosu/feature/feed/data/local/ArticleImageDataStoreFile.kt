package io.github.omochice.pinosu.feature.feed.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin

/**
 * A file the article image lookups are stored in, opened with a real DataStore the way the app
 * opens it
 *
 * Opening it again stands for a later run of the app, which knows only what the file holds.
 */
internal class ArticleImageDataStoreFile {

  private val directory: File = Files.createTempDirectory("article-images").toFile()

  /** The stored file itself */
  val file = File(directory, "feed_article_images.json")

  private var job: Job? = null

  /** Open the file, closing the DataStore opened before so that only one is active */
  suspend fun open(): DataStore<ArticleImageLookups> {
    job?.cancelAndJoin()
    val newJob = SupervisorJob()
    job = newJob
    return DataStoreFactory.create(
        serializer = ArticleImageLookupsSerializer,
        scope = CoroutineScope(Dispatchers.IO + newJob),
        produceFile = { file })
  }

  /** Close the DataStore and delete the file */
  suspend fun delete() {
    job?.cancelAndJoin()
    directory.deleteRecursively()
  }
}
