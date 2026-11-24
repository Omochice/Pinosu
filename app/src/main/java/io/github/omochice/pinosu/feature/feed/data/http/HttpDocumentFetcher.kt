package io.github.omochice.pinosu.feature.feed.data.http

import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody

/** Downloads a document with a GET request, refusing error statuses and oversized bodies */
class HttpDocumentFetcher @Inject constructor(private val okHttpClient: OkHttpClient) {

  /**
   * Download the document at [url]
   *
   * @param url Absolute `http://` or `https://` URL
   * @return The document, or a failure with [HttpStatusException] for a non-2xx status, with
   *   [ResponseTooLargeException] when the body exceeds [MAX_BODY_BYTES], with
   *   [IllegalArgumentException] when [url] is not a valid HTTP(S) URL, or with an [IOException]
   *   when the transfer fails
   */
  suspend fun fetch(url: String): Result<FetchedDocument> =
      withContext(Dispatchers.IO) {
        try {
          val request = request(url)
          okHttpClient.newCall(request).execute().use { read(request.url.toString(), it) }
        } catch (e: IOException) {
          Result.failure(e)
        } catch (e: IllegalArgumentException) {
          Result.failure(e)
        }
      }

  private fun request(url: String): Request =
      Request.Builder().url(url).header("User-Agent", USER_AGENT).header("Accept", ACCEPT).build()

  private fun read(requestedUrl: String, response: Response): Result<FetchedDocument> =
      if (response.isSuccessful) {
        readSuccessful(requestedUrl, response)
      } else {
        Result.failure(HttpStatusException(response.code))
      }

  private fun readSuccessful(requestedUrl: String, response: Response): Result<FetchedDocument> {
    val body =
        readCapped(response.body)
            ?: return Result.failure(ResponseTooLargeException(MAX_BODY_BYTES))
    return Result.success(
        FetchedDocument(
            requestedUrl = requestedUrl,
            finalUrl = response.request.url.toString(),
            body = body,
            charset = response.body.contentType()?.charset()?.name(),
        ))
  }

  private fun readCapped(body: ResponseBody): ByteArray? {
    if (body.contentLength() > MAX_BODY_BYTES) return null
    val source = body.source()
    return if (source.request(MAX_BODY_BYTES + 1)) null else source.readByteArray()
  }

  companion object {
    private const val USER_AGENT = "Pinosu/1.0 (Android)"
    private const val ACCEPT =
        "application/rss+xml, application/atom+xml, application/rdf+xml;q=0.9, " +
            "application/xml;q=0.8, text/xml;q=0.8, text/html;q=0.7, */*;q=0.1"

    /** Largest response body accepted, in bytes */
    internal const val MAX_BODY_BYTES = 5L * 1024 * 1024
  }
}
