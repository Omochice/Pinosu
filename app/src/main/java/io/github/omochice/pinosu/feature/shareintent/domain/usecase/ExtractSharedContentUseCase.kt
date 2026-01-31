package io.github.omochice.pinosu.feature.shareintent.domain.usecase

import android.content.Intent
import io.github.omochice.pinosu.feature.shareintent.domain.model.SharedContent

/**
 * Use case interface for extracting shared content from an incoming intent
 *
 * Examines an [Intent] to determine if it carries ACTION_SEND text/plain data and extracts the
 * shared text, classifying it as either a URL or a comment.
 */
interface ExtractSharedContentUseCase {

  /**
   * Extract shared content from the given intent
   *
   * @param intent The incoming intent, or null
   * @return [SharedContent] if the intent is a valid ACTION_SEND text/plain with non-blank text,
   *   null otherwise
   */
  operator fun invoke(intent: Intent?): SharedContent?
}
