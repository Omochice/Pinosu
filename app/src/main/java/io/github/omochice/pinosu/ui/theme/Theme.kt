package io.github.omochice.pinosu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme()

private val DarkColorScheme = darkColorScheme()

/**
 * Pinosu app theme
 *
 * Supports ThemeMode-based dark/light switching and Material You dynamic colors on Android 12+.
 *
 * @param themeMode Theme mode preference (System, Light, or Dark)
 * @param dynamicColor Whether to use Material You dynamic colors on supported devices
 * @param content Composable content to be themed
 */
@Composable
fun PinosuTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
  val darkTheme =
      when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
      }

  val colorScheme =
      when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
          DarkColorScheme
        }
        else -> {
          LightColorScheme
        }
      }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val activity = view.context as Activity
      WindowInsetsControllerCompat(activity.window, view).apply {
        isAppearanceLightStatusBars = !darkTheme
        isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(colorScheme = colorScheme, content = content)
}
