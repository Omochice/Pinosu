package io.github.omochice.pinosu.feature.feed.data.http

import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * A document downloaded over HTTP, held in memory so that it can be read more than once
 *
 * @param body Raw bytes of the document
 * @property requestedUrl URL the request was sent to
 * @property finalUrl URL the document was served from after redirects, which relative links in it
 *   are resolved against
 * @property charset Charset named by the Content-Type header, or null when it names none
 */
class FetchedDocument(
    private val body: ByteArray,
    val requestedUrl: String,
    val finalUrl: String,
    val charset: String?,
) {

  /** A fresh stream over the body */
  fun openBody(): InputStream = ByteArrayInputStream(body)
}
