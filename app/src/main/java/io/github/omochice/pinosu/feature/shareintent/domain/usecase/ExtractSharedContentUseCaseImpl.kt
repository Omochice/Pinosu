package io.github.omochice.pinosu.feature.shareintent.domain.usecase

import android.content.Intent
import io.github.omochice.pinosu.feature.shareintent.domain.model.SharedContent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [ExtractSharedContentUseCase]
 *
 * Extracts shared text from ACTION_SEND text/plain intents and classifies it as either a URL (if it
 * starts with http:// or https://) or a comment (otherwise).
 */
@Singleton
class ExtractSharedContentUseCaseImpl @Inject constructor() : ExtractSharedContentUseCase {

  override operator fun invoke(intent: Intent?): SharedContent? {
    if (intent == null) return null
    if (intent.action != Intent.ACTION_SEND) return null
    if (intent.type != "text/plain") return null

    val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
    if (text.isNullOrBlank()) return null

    return if (text.startsWith("http://", ignoreCase = true) ||
        text.startsWith("https://", ignoreCase = true)) {
      SharedContent(url = text)
    } else {
      SharedContent(comment = text)
    }
  }
}
