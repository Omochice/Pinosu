package io.github.omochice.pinosu

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt instrumented tests.
 *
 * Hilt tests cannot use the production @HiltAndroidApp application (PinosuApplication). This runner
 * replaces it with HiltTestApplication, which is designed for testing.
 */
class HiltTestRunner : AndroidJUnitRunner() {
  override fun newApplication(
      cl: ClassLoader?,
      className: String?,
      context: Context?,
  ): Application {
    return super.newApplication(cl, HiltTestApplication::class.java.name, context)
  }
}
