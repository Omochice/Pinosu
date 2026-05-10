package io.github.omochice.pinosu.manifest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestConfigurationTest {

  private lateinit var context: Context
  private lateinit var packageManager: PackageManager

  @BeforeTest
  fun setup() {
    context = InstrumentationRegistry.getInstrumentation().targetContext
    packageManager = context.packageManager
  }

  @Test
  fun `application name should not be null or empty`() {
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    val appName = packageManager.getApplicationLabel(applicationInfo).toString()

    assertNotNull(appName, "Application name should not be null")
    assertTrue("Application name should not be empty", appName.isNotEmpty())
  }

  @Test
  fun `application icon should be set`() {
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    val appIcon = applicationInfo.icon

    assertTrue("Application icon should be set", appIcon != 0)
  }

  @Test
  fun `queries element for Nip55Signer intent should be configured`() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"))

    val resolveInfo =
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

    assertNotNull(resolveInfo, "Query for nostrsigner scheme should be possible")
  }

  @Test
  fun `MainActivity should be exported for launcher intent`() {
    val intent =
        Intent(Intent.ACTION_MAIN).apply {
          addCategory(Intent.CATEGORY_LAUNCHER)
          setPackage(context.packageName)
        }

    val resolveInfo = packageManager.queryIntentActivities(intent, 0).firstOrNull()

    assertNotNull(resolveInfo, "MainActivity should be resolvable")

    val activityInfo = resolveInfo?.activityInfo
    assertNotNull(activityInfo, "ActivityInfo should not be null")
    assertTrue(
        "MainActivity should be exported for LAUNCHER intent", activityInfo?.exported == true)
  }

  @Test
  fun `MainActivity should resolve ACTION_SEND for text plain`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          setPackage(context.packageName)
        }

    val resolveInfo = packageManager.queryIntentActivities(intent, 0)

    assertTrue("ACTION_SEND text/plain should resolve to an activity", resolveInfo.isNotEmpty())
  }
}
