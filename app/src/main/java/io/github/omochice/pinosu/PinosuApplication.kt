package io.github.omochice.pinosu

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Pinosu application class
 * - Hilt DI container initialization
 *
 * The @HiltAndroidApp annotation triggers Hilt's code generation, making DI available throughout
 * the application.
 */
@HiltAndroidApp class PinosuApplication : Application()
