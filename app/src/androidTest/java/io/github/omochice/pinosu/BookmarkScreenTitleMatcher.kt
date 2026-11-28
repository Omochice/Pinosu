package io.github.omochice.pinosu

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText

/**
 * Matches the title of the bookmark screen
 *
 * The drawer's Bookmarks entry shows the same text and stays in the semantics tree while the drawer
 * is closed, but unlike the title it is clickable.
 */
fun bookmarkScreenTitle(): SemanticsMatcher =
    hasText(getTestString(R.string.title_bookmarks)) and !hasClickAction()
