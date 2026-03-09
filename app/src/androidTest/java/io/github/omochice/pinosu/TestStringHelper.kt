package io.github.omochice.pinosu

import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Resolve a string resource using the target app context.
 *
 * Ensures test assertions match the displayed text regardless of device locale.
 */
fun getTestString(@StringRes resId: Int): String =
    InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)
