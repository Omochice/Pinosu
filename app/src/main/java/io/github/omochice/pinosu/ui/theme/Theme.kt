package io.github.omochice.pinosu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme()

private val DarkColorScheme = darkColorScheme()

/**
 * Pinosu app theme
 *
 * Uses Material3's default color scheme
 */
@Composable
fun PinosuTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, content = content)
}
