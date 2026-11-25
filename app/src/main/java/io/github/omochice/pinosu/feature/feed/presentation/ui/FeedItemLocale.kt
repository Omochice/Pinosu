package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * Locale the text of [this] is written in, without which Android applies the phrase word break only
 * when the device itself is set to Japanese
 *
 * @return The locale the feed declares, or null to fall back to the device locale
 */
internal fun FeedItem.localeList(): LocaleList? = language?.let { LocaleList(Locale(it)) }
