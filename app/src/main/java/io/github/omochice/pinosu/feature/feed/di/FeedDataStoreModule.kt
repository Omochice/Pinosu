package io.github.omochice.pinosu.feature.feed.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.feed.data.local.ArticleImageLookups
import io.github.omochice.pinosu.feature.feed.data.local.ArticleImageLookupsSerializer
import java.io.File
import javax.inject.Singleton

/** Hilt module for what the feed feature stores with DataStore */
@Module
@InstallIn(SingletonComponent::class)
object FeedDataStoreModule {

  private const val ARTICLE_IMAGES_FILE = "feed_article_images.json"

  /**
   * The file is in the no-backup directory rather than the files directory, because the lookups can
   * be repeated and are not worth restoring on another device, and not in the cache directory,
   * which the system may clear while the file is open.
   *
   * A corrupt file is replaced rather than reported as the auth data is, for the same reason.
   */
  @Provides
  @Singleton
  fun provideArticleImageDataStore(
      @ApplicationContext context: Context
  ): DataStore<ArticleImageLookups> =
      DataStoreFactory.create(
          serializer = ArticleImageLookupsSerializer,
          corruptionHandler = ReplaceFileCorruptionHandler { ArticleImageLookups() },
          produceFile = { File(context.noBackupFilesDir, ARTICLE_IMAGES_FILE) })
}
