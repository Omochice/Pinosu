package io.github.omochice.pinosu.feature.shareintent.domain.usecase

/**
 * Narrows a greedily-matched URL [range] within [text] to its true boundary by dropping trailing
 * characters that are punctuation around the URL rather than part of it. `\S+` greedily swallows a
 * trailing `),.;` etc. (e.g. `"(https://example.com/a), ..."`), which would otherwise be saved as a
 * broken URL. A closing bracket is only dropped when it is unbalanced within the match so that URLs
 * legitimately containing balanced brackets (e.g. Wikipedia `..._(protocol)` links) are preserved.
 */
internal fun trimmedUrlRange(text: String, range: IntRange): IntRange =
    range.first..trailingTrimmedEnd(text, range)

private fun trailingTrimmedEnd(text: String, range: IntRange): Int {
  var end = range.last
  while (end > range.first && shouldDropTrailing(text[end], text, range.first, end)) {
    end--
  }
  return end
}

private fun shouldDropTrailing(c: Char, text: String, start: Int, end: Int): Boolean =
    c in TRAILING_PUNCTUATION ||
        CLOSING_TO_OPENING[c]?.let { open ->
          val sub = text.substring(start..end)
          sub.count { it == open } < sub.count { it == c }
        } ?: false

private val TRAILING_PUNCTUATION = setOf('.', ',', ';', ':', '!', '?', '"', '\'')
private val CLOSING_TO_OPENING = mapOf(')' to '(', ']' to '[', '}' to '{')
