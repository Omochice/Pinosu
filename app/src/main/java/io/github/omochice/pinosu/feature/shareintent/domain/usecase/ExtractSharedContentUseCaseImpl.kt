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
      val url = urlMatch.value
      val remaining = text.removeRange(urlMatch.range).trim().replace(Regex("\\s{2,}"), " ")
      SharedContent(url = url, comment = remaining.ifBlank { null })
    } else {
      SharedContent(comment = text)
    }
  }

  companion object {
    private val URL_PATTERN = """https?://\S+""".toRegex(RegexOption.IGNORE_CASE)
  }
}
