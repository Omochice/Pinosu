package io.github.omochice.pinosu.feature.feed.data.parser

/**
 * The document is not an RSS 2.0, RSS 1.0, or Atom feed
 *
 * @param rootName Name of the document's root element, or null when it has none
 */
class UnsupportedFeedFormatException(rootName: String?) :
    Exception("Not a feed document: root element is ${rootName ?: "missing"}")
