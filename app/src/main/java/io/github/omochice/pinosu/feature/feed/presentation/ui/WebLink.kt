package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.ui.platform.UriHandler

private val WEB_SCHEMES = listOf("http://", "https://")

/**
 * Whether [url] is an HTTP(S) URL
 *
 * Feed links are checked again here, because any other scheme would hand control to another app.
 */
internal fun isWebLink(url: String): Boolean =
    WEB_SCHEMES.any { url.startsWith(it, ignoreCase = true) }

/**
 * Open [url] in the browser when it is an HTTP(S) URL
 *
 * @return Whether the URL was handed to the browser
 */
internal fun openWebLink(uriHandler: UriHandler, url: String): Boolean {
  if (!isWebLink(url)) return false
  return try {
    uriHandler.openUri(url)
    true
  } catch (_: IllegalArgumentException) {
    false
  }
}
