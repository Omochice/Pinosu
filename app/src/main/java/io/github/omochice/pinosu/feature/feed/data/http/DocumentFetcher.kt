package io.github.omochice.pinosu.feature.feed.data.http

import okhttp3.CacheControl

/** Downloads a document over HTTP */
interface DocumentFetcher {

  /**
   * Download the document at [url]
   *
   * @param url Absolute URL of the document
   * @param cacheControl Cache directives sent with the request, or null to send none
   * @return The document, or a failure telling why it could not be downloaded
   */
  suspend fun fetch(url: String, cacheControl: CacheControl? = null): Result<FetchedDocument>
}
