package io.github.omochice.pinosu.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Abstraction for text that can originate from either a string resource or a dynamic string.
 *
 * Allows ViewModels to emit user-visible text without depending on Android Context.
 */
@Suppress("SpreadOperator")
sealed class UiText {
  data class DynamicString(val value: String) : UiText()

  class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText()

  @Composable
  fun asString(): String {
    return when (this) {
      is DynamicString -> value
      is StringResource -> stringResource(resId, *args)
    }
  }

  fun asString(context: Context): String {
    return when (this) {
      is DynamicString -> value
      is StringResource -> context.getString(resId, *args)
    }
  }
}
