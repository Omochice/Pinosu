package io.github.omochice.pinosu

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.test
import roidx.test.ext.junit.runners.AndroidJUnit4
import roidx.test.platform.app.InstrumentationRegistry

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d. roid.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedtest {
  @test
  fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    assertEquals("io.github.omochice.pinosu", appContext.packageName)
  }
}
