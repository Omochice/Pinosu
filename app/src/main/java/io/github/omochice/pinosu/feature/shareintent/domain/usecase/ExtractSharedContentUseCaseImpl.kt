package io.github.omochice.pinosu.feature.shareintent.domain.usecase

import android.content.ClipDescription
import android.content.Intent
import io.github.omochice.pinosu.feature.shareintent.domain.model.SharedContent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [ExtractSharedContentUseCase]
 *
 * Extracts shared text from ACTION_SEND text/plain intents. Uses regex to find the first URL
 * anywhere in the text, extracting it as [SharedContent.url] and the surrounding text as
 * [SharedContent.comment].
 */
@Singleton
class ExtractSharedContentUseCaseImpl @Inject constructor() : ExtractSharedContentUseCase {

  override operator fun invoke(intent: Intent?): SharedContent? {
    if (intent == null) return null
    if (intent.action != Intent.ACTION_SEND) return null
    if (!ClipDescription.compareMimeTypes(
        intent.type?.substringBefore(';')?.trim() ?: "", "text/plain")) {
      return null
    }

    val text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.trim()
    if (text.isNullOrBlank()) return null

    val urlMatch = URL_PATTERN.find(text)
    return if (urlMatch != null) {
      val urlRange = urlMatch.range.first..trailingTrimmedEnd(text, urlMatch.range)
      val url = text.substring(urlRange)
      val remaining = text.removeRange(urlRange).trim().replace(Regex("\\s{2,}"), " ")
      SharedContent(url = url, comment = remaining.ifBlank { null })
    } else {
      SharedContent(comment = text)
    }
  }

  /**
   * Returns the inclusive end index of the URL within [range] after dropping trailing characters
   * that are punctuation surrounding the URL rather than part of it. `\S+` greedily swallows a
   * trailing `),.;` etc. (e.g. `"(https://example.com/a), ..."`), which would otherwise be saved as
   * a broken URL. A closing bracket is only dropped when it is unbalanced within the match so that
   * URLs legitimately containing balanced brackets are preserved.
   */
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

  companion object {
    private val URL_PATTERN = """https?://\S+""".toRegex(RegexOption.IGNORE_CASE)
    private val TRAILING_PUNCTUATION = setOf('.', ',', ';', ':', '!', '?', '"', '\'')
    private val CLOSING_TO_OPENING = mapOf(')' to '(', ']' to '[', '}' to '{')
  }
}
